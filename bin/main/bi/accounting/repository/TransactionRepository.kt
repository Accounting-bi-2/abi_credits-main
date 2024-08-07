package bi.accounting.repository

import bi.accounting.model.Transaction
import io.micronaut.data.annotation.Query
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@R2dbcRepository(dialect = Dialect.POSTGRES)
interface TransactionRepository: ReactorCrudRepository<Transaction, Long> {
    override fun <S : Transaction> save(transaction: S): Mono<S>
    @Query("SELECT * FROM credits_schema.transaction WHERE user_id = :userId AND transaction_date >= :date ORDER BY id DESC LIMIT :n")
    fun findLatestNByUserId(userId: Long, n: Int, date: LocalDateTime): Flux<Transaction>
}