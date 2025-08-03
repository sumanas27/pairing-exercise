package io.billie.shipment.application.dto

import java.math.BigDecimal
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Digits
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class CreateShipmentRequest(
    @field:NotBlank(message = "Order ID cannot be blank")
    @field:Size(max = 100, message = "Order ID cannot exceed 100 characters")
    val orderId: String,

    @field:NotNull(message = "Amount is required")
    @field:DecimalMin(value = "0.01", message = "Amount must be positive")
    @field:Digits(integer = 10, fraction = 2, message = "Invalid amount format")
    val amount: BigDecimal,

    @field:NotBlank(message = "Currency is required")
    @field:Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
    @field:Pattern(regexp = "[A-Z]{3}", message = "Currency must be uppercase ISO 4217 code")
    val currency: String,

    @field:Size(max = 255, message = "Tracking number cannot exceed 255 characters")
    val trackingNumber: String? = null
)
