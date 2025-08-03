package io.billie.shipment.application.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import java.math.BigDecimal

@JsonInclude(NON_NULL)
data class CreateShipmentResponse(
    val success: Boolean,
    val shipmentId: String? = null,
    val transactionId: String? = null,
    val remainingAmount: BigDecimal? = null,
    val currency: String? = null,
    val errorMessage: String? = null
)