package io.billie.shipment.domain.service

data class PaymentResult(
    val success: Boolean,
    val transactionId: String? = null,
    val errorMessage: String? = null
)
