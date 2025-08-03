package io.billie.shipment.application.result

import io.billie.shipment.domain.model.valueobjects.Money
import io.billie.shipment.domain.model.valueobjects.ShipmentId
import io.billie.shipment.domain.service.PaymentResult

data class ShipmentResult(
    val shipmentId: ShipmentId,
    val paymentResult: PaymentResult,
    val remainingAmount: Money
)