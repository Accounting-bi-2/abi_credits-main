package bi.accounting.dto

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class PurchaseRequest(
    val amount : Double,
    val currency: String
) {
}