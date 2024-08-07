package bi.accounting.model

import io.micronaut.data.annotation.*
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Serdeable
@MappedEntity
data class Credit(

    @field:Id
    @field:GeneratedValue(GeneratedValue.Type.SEQUENCE)
    var id: Long? = null,
    var expiryDate: Date? = null,
    var amount: @NotNull @NotBlank BigDecimal? = null,
    var userId: @NotNull @NotBlank Long? = null,
    var transactionId: Long? = null,
    @DateCreated var dateCreated: OffsetDateTime? = null,
    @DateUpdated var dateUpdated: OffsetDateTime? = null
)