package bi.accounting.model

import bi.accounting.enums.TransactionType
import io.micronaut.data.annotation.*
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

@Serdeable
@MappedEntity
data class Transaction(

    @field:Id
    @field:GeneratedValue(GeneratedValue.Type.SEQUENCE)
    var id: Long? = null,
    var amount: @NotNull @NotBlank BigDecimal? = null,
    var userId: @NotNull @NotBlank Long? = null,
    var conversionrate: @NotNull @NotBlank Double? = null,
    var type: TransactionType? = null,
    var discountAmount: BigDecimal? = null,
    var description: String? = null,
    var discountDescription: String? = null,
    var expiryDate: @NotNull @NotBlank Date? = null,
    var transactionDate: @NotNull @NotBlank Date? = null,
    var purchaseId: Long? = null,
    @DateCreated var dateCreated: OffsetDateTime? = null,
    @DateUpdated var dateUpdated: OffsetDateTime? = null
)