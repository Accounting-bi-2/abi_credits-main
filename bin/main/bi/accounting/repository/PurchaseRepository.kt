package bi.accounting.repository

import bi.accounting.enums.PurchaseStatus
import bi.accounting.model.Purchase
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import reactor.core.publisher.Mono

@R2dbcRepository(dialect = Dialect.POSTGRES)
interface PurchaseRepository: ReactorCrudRepository<Purchase, Long> {

    fun findByStatusAndUserId(status: PurchaseStatus, userId: Long): Mono<Purchase>
    fun findByStripeSessionId(stripeSessionId: String): Mono<Purchase>
}