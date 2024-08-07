package bi.accounting.service

import bi.accounting.model.UserCreditsView
import bi.accounting.repository.TransactionRepository
import bi.accounting.repository.UserCreditViewRepository
import jakarta.inject.Singleton
import reactor.core.publisher.Flux
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

@Singleton
class UserCreditService(
    private val userCreditViewRepository: UserCreditViewRepository,
    private val transactionRepository: TransactionRepository
) {
    fun findUserCredits(userId: Long): Flux<UserCreditsView> {
        return userCreditViewRepository.findByUserId(userId)
            .flatMap { userCreditView ->
                val startOfMonth = LocalDateTime.now()
                    .with(TemporalAdjusters.firstDayOfMonth())
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0)
                transactionRepository.findLatestNByUserId(userId, 10, startOfMonth)
                    .collectList()
                    .map { transactions ->
                        var userCreditsView = UserCreditsView(
                            userId = userCreditView.userId,
                            currentCredits = userCreditView.currentCredits,
                        )
                        userCreditsView.transactionsList = transactions
                        userCreditsView
                    }
            }
    }
}