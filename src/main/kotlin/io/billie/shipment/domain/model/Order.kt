package io.billie.shipment.domain.model

import io.billie.shipment.domain.model.valueobjects.MerchantId
import io.billie.shipment.domain.model.valueobjects.Money
import io.billie.shipment.domain.model.valueobjects.OrderId
import java.math.BigDecimal
import java.time.Instant

data class Order(
    val id: OrderId,
    val merchantId: MerchantId,
    val totalAmount: Money,
    val status: OrderStatus,
    val createdAt: Instant,
    val shipments: MutableList<Shipment> = mutableListOf()
) {
    fun getTotalShippedAmount(): Money {
        if (shipments.isEmpty()) {
            return Money(BigDecimal.ZERO, totalAmount.currency)
        }
        return shipments
            .map { it.amount }
            .reduce { acc, amount -> acc + amount }
    }

    fun getRemainingAmount(): Money {
        return totalAmount - getTotalShippedAmount()
    }

    fun canAcceptShipment(shipmentAmount: Money): Boolean {
        require(shipmentAmount.currency == totalAmount.currency) {
            "Shipment currency must match order currency"
        }
        return getTotalShippedAmount() + shipmentAmount <= totalAmount
    }

    fun addShipment(shipment: Shipment): Order {
        require(canAcceptShipment(shipment.amount)) {
            "Shipment amount would exceed order total"
        }
        require(shipment.orderId == this.id) {
            "Shipment must belong to this order"
        }

        shipments.add(shipment)
        val newStatus = when {
            getRemainingAmount().isZero() -> OrderStatus.FULLY_SHIPPED
            getRemainingAmount().isGreaterThanZero() -> OrderStatus.PARTIALLY_SHIPPED
            else -> status
        }

        return copy(shipments = shipments, status = newStatus)
    }

    fun isFullyShipped(): Boolean = status == OrderStatus.FULLY_SHIPPED
}