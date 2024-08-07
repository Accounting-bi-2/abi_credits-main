package bi.accounting.dto

import bi.accounting.enums.TransactionType
import io.micronaut.core.annotation.Introspected
import io.micronaut.core.annotation.NonNull
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

@Serdeable
@Introspected
data class TransactionRequest(
    val amount: @NonNull BigDecimal,
    val description: String? = null,
    val type: @NonNull @NotBlank TransactionType,
    val conversionrate: @NonNull @NotBlank Double
)