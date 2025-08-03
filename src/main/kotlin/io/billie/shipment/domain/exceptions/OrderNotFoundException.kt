package io.billie.shipment.domain.exceptions

import io.billie.shipment.domain.model.valueobjects.OrderId

class OrderNotFoundException(orderId: OrderId) :
    RuntimeException("Order not found: ${orderId.value}")