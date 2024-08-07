package bi.accounting.model

import io.micronaut.core.annotation.Introspected
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.Transient
import io.micronaut.serde.annotation.Serdeable
import java.math.BigDecimal

@Serdeable
@MappedEntity
@Introspected
data class UserCreditsView(
    @field:Id
    val userId: Long,
    val currentCredits: BigDecimal,
){
    @Transient
    var transactionsList: List<Transaction>? = null
}