package bi.accounting.repository

import bi.accounting.model.UserCreditsView
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorCrudRepository
import reactor.core.publisher.Flux

@R2dbcRepository(dialect = Dialect.POSTGRES)
interface UserCreditViewRepository: ReactorCrudRepository<UserCreditsView, Long> {

    fun findByUserId(userId: Long): Flux<UserCreditsView>
}