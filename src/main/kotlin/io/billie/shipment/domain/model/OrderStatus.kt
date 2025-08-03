package io.billie.shipment.domain.model

enum class OrderStatus {
    PENDING,
    PARTIALLY_SHIPPED,
    FULLY_SHIPPED,
    CANCELLED
}