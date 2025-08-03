package io.billie.shipment.domain.repository

import io.billie.shipment.domain.model.Order
import io.billie.shipment.domain.model.valueobjects.OrderId

interface OrderRepository {
    fun findById(orderId: OrderId): Order?
    fun save(order: Order): Order
}
