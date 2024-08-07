package bi.accounting.service

import bi.accounting.enums.TransactionType
import bi.accounting.model.Credit
import bi.accounting.model.Transaction
import bi.accounting.model.UserCreditsView
import bi.accounting.repository.CreditRepository
import bi.accounting.repository.TransactionRepository
import io.micronaut.transaction.annotation.Transactional
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC
import java.time.temporal.TemporalAdjusters
import java.util.*
import java.util.Calendar.*

@Singleton
open class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val creditRepository: CreditRepository,
    private val userCreditService: UserCreditService
) {

    @Transactional
    open fun addTransaction(userId: Long, amount: BigDecimal, type: TransactionType, conversionrate: Double,
                            purchaseId: Long?, description: String?): Flux<Transaction> {

        if(amount <= BigDecimal.ZERO) {
            return Flux.error(IllegalArgumentException("Amount must be greater than zero"))
        }

        return userCreditService.findUserCredits(userId)
            .switchIfEmpty(Mono.just(UserCreditsView(userId, BigDecimal.ZERO)))
            .flatMap { userCredit ->

                if (type == TransactionType.DEBIT && userCredit.currentCredits < amount) {
                    Mono.error(IllegalArgumentException("User does not have enough credits"))
                } else {

                    val transaction = Transaction(
                        userId = userId,
                        amount = amount,
                        type = type,
                        conversionrate = conversionrate,
                        purchaseId = purchaseId,
                        transactionDate = Date(),
                        description = description,
                        expiryDate = Date().let {
                            val calendar = getInstance()
                            calendar.time = it
                            calendar.set(HOUR_OF_DAY, 23)
                            calendar.set(MINUTE, 59)
                            calendar.set(SECOND, 59)
                            calendar.set(MILLISECOND, 999)
                            calendar.time
                        }
                    )
                    val credit = Credit(
                        userId = userId,
                        amount = if (type == TransactionType.CREDIT) userCredit.currentCredits + amount else userCredit.currentCredits - amount,
                        expiryDate = transaction.expiryDate
                    )

                    transactionRepository.save(transaction)
                        .flatMap { savedTransaction ->
                            credit.transactionId = savedTransaction.id
                            creditRepository.save(credit)
                                .thenReturn(savedTransaction)
                        }
                }
            }
            .doOnError { error -> LOG.error("Error: $error") }
    }

    fun getTransactions(userId: Long, n: Int): Flux<Transaction> {
        val startOfMonth = LocalDateTime.now()
            .with(TemporalAdjusters.firstDayOfMonth())
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        return transactionRepository.findLatestNByUserId(userId, n, startOfMonth)
    }

    companion object {
        val LOG: Logger = LoggerFactory.getLogger("TransactionService")
    }
}