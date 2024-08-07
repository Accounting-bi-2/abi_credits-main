package bi.accounting.model

import bi.accounting.enums.PurchaseStatus
import io.micronaut.data.annotation.*
import io.micronaut.data.annotation.sql.JoinColumn
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.OffsetDateTime

@MappedEntity
class Purchase (
    @field:Id
    @field:GeneratedValue
    var id: Long? = null,

    var userId: Long? = null, // Assuming there is a User entity that you can link to.

    var status: PurchaseStatus = PurchaseStatus.PENDING, // Default to PENDING when created.

    var amount: BigDecimal,

    var currency: String,

    var stripeSessionId: String? = null, // The ID of the Stripe session.

    @field:DateCreated
    var dateCreated: OffsetDateTime? = null,

    @field:DateUpdated
    var dateUpdated: OffsetDateTime? = null,

    var sessionUrl: String? = null // The URL of the Stripe session.
)