package io.billie.shipment.domain.model

import io.billie.shipment.domain.model.valueobjects.Money
import io.billie.shipment.domain.model.valueobjects.OrderId
import io.billie.shipment.domain.model.valueobjects.ShipmentId
import java.math.BigDecimal
import java.time.Instant

data class Shipment(
    val id: ShipmentId,
    val orderId: OrderId,
    val amount: Money,
    val shippedAt: Instant,
    val trackingNumber: String? = null
) {
    init {
        require(amount.amount > BigDecimal.ZERO) { "Shipment amount must be positive" }
    }
}