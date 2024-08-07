package bi.accounting.service

import bi.accounting.config.CreditsConfig
import bi.accounting.enums.PurchaseStatus
import bi.accounting.enums.TransactionType
import bi.accounting.model.Purchase
import bi.accounting.repository.PurchaseRepository
import com.stripe.Stripe
import com.stripe.model.Charge
import com.stripe.model.Event
import com.stripe.model.PaymentIntent
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import com.stripe.param.checkout.SessionCreateParams
import io.micronaut.context.annotation.Value
import io.micronaut.serde.ObjectMapper
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Singleton
open class PurchaseService(
    private val purchaseRepository: PurchaseRepository,
    private val transactionService: TransactionService,
    private val creditsConfig: CreditsConfig,
    @Value("\${stripe.api-key}") var apiKey: String? = null,
    @Value("\${stripe.webhook-secret}") var endpointSecret: String? = null,
    @Value("\${micronaut.server.base-url}") var baseUrl: String? = null,
    private val objectMapper: ObjectMapper
) {

    fun createCheckoutSession(userId: Long, amount: Double, currency: String): Mono<String> {
        Stripe.apiKey = apiKey
        val serverUrl = baseUrl
        return purchaseRepository.findByStatusAndUserId(PurchaseStatus.PENDING, userId)
            .switchIfEmpty(Mono.defer {
                val params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("$serverUrl/credits-service/credits/purchase/success") // Replace with your success URL
                    .setCancelUrl("$serverUrl/credits-service/credits/purchase/cancel")   // Replace with your cancel URL
                    .setExpiresAt((System.currentTimeMillis() / 1000L + 1800))
                    .addLineItem(
                        SessionCreateParams.LineItem.builder()
                            .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(currency)
                                    .setUnitAmount(amount.toLong()) // Price in cents
                                    .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("BI Credits")
                                            .build()
                                    )
                                    .build()
                            )
                            .setQuantity(1L)
                            .build()
                    )
                    .build()

                val session = Session.create(params)
                purchaseRepository.save(Purchase(
                    userId = userId,
                    status = PurchaseStatus.PENDING,
                    stripeSessionId = session.id,
                    amount = amount.div(100).toBigDecimal(),
                    currency = currency.uppercase(),
                    sessionUrl = session.url
                ))
            })
            .flatMap { purchase ->
                Mono.just(purchase.sessionUrl!!)
            }
    }

    fun webhook(payload: String, sig: String): Mono<String> {
        Stripe.apiKey = apiKey
        val event: Event = Webhook.constructEvent(payload, sig, endpointSecret)
        // Handle the checkout.session.completed event
        Log.debug("webhook event type {} data", event.type)
        if ("checkout.session.completed" == event.type || "checkout.session.expired" == event.type) {
            val session = event.data.`object` as Session
            // Fulfill the purchase...
            Log.info("webhook status {}",session.paymentStatus)

            return purchaseRepository.findByStripeSessionId(session.id)
                .flatMap { purchase ->

                    if (session.paymentStatus == "paid") {

                        val paymentIntentId = session.paymentIntent
                        val paymentIntent: PaymentIntent = PaymentIntent.retrieve(paymentIntentId)
                        val charge: Charge = paymentIntent.charges.data[0]
//                        Log.info("receipt url {}", charge.receiptUrl)
                        purchase.receiptUrl = charge.receiptUrl
                        purchase.status = PurchaseStatus.SUCCESS
                        updatePaymentAddCredits(purchase)
                            .thenReturn("Purchase Success")
                    } else {
                        purchase.status = PurchaseStatus.FAILED
                        purchaseRepository.update(purchase)
                            .thenReturn("Purchase Failed")
                    }
                 }
        }else{
            return Mono.just("Unhandled Event Type")
        }
    }

    @Transactional
    open fun updatePaymentAddCredits(purchase: Purchase): Mono<Purchase> {
        return purchaseRepository.update(purchase)
            .flatMap { updatedPurchase ->
                transactionService.addTransaction(
                    userId = updatedPurchase.userId!!,
                    amount = convertToCredit(updatedPurchase.currency, updatedPurchase.amount),
                    conversionrate = creditsConfig.rate[updatedPurchase.currency.lowercase()]!!,
                    type = TransactionType.CREDIT,
                    purchaseId = updatedPurchase.id,
                    description = "Purchase of ${updatedPurchase.amount} ${updatedPurchase.currency}"
                )
                    .collectList()
                    .map {
                        updatedPurchase
                    }
            }
    }

    fun listPurchase(userId: Long): Flux<Purchase> {
        return purchaseRepository.findByUserId(userId)
    }

    private fun convertToCredit(currency: String, amount: BigDecimal): BigDecimal {
        val rate = creditsConfig.rate[currency.lowercase()] ?: throw IllegalArgumentException("Currency rate not found")
        return amount.multiply(rate.toBigDecimal())
    }

    companion object{
        val Log = LoggerFactory.getLogger(PurchaseService::class.java)
    }
}