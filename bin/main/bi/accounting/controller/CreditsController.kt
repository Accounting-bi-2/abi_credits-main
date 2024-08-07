package bi.accounting.controller

import bi.accounting.dto.PurchaseRequest
import bi.accounting.dto.TransactionRequest
import bi.accounting.model.Transaction
import bi.accounting.model.UserCreditsView
import bi.accounting.service.PurchaseService
import bi.accounting.service.TransactionService
import bi.accounting.service.UserCreditService
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI

@Controller("/credits")
class CreditsController(
    private val userCreditService: UserCreditService,
    private val transactionService: TransactionService,
    private val purchaseService: PurchaseService
) {

    @Value("\${uri.payment-success}")
    private lateinit var paymentSuccessUrl: String

    @Get(uri="/", produces=["text/plain"])
    fun index(): String {
        return "Example Response"
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get(uri="/balance")
    fun getCredits(@Header("X-UserID") userId: Long): Flux<UserCreditsView> {
        return userCreditService.findUserCredits(userId)
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Post(uri="/transaction")
    fun newTransaction(@Header("X-UserID") userId: Long, @Body transactionRequest: TransactionRequest): Mono<Transaction> {
        return transactionService.addTransaction(userId, transactionRequest.amount, transactionRequest.type, transactionRequest.conversionrate,
            null, transactionRequest.description).collectList().map { it[0] }
    }

    @Get("/transaction")
    fun getTransactions(@Header("X-UserID") userId: Long, @QueryValue("limit") limit: Int): MutableHttpResponse<Flux<Transaction>>? {
        return HttpResponse.ok(transactionService.getTransactions(userId, limit))
    }

    @Post("/purchase")
    fun purchase(@Header("X-UserID") userId: Long, @Body purchaseRequest: PurchaseRequest): MutableHttpResponse<URI>? {
        val url = purchaseService.createCheckoutSession(userId, purchaseRequest.amount, purchaseRequest.currency)
        return HttpResponse.ok(url.block()?.let { URI.create(it) })
    }

    @Get("/purchase/success")
    fun purchaseSuccess(): MutableHttpResponse<String>? {
        return HttpResponse.seeOther(URI.create(paymentSuccessUrl))
    }

    @Get("/purchase/cancel")
    fun purchaseCancel(): MutableHttpResponse<String>? {
        return HttpResponse.seeOther(URI.create(paymentSuccessUrl))
    }

    @Post("/purchase/webhook")
    fun webhook(@Body payload: String, @Header("Stripe-Signature") sig: String): MutableHttpResponse<String>? {
        purchaseService.webhook(payload, sig).block()
        return HttpResponse.ok<Unit?>().body("Webhook received")
    }
}