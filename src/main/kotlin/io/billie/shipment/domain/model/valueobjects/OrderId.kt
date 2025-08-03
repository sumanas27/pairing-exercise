package io.billie.shipment.domain.model.valueobjects

data class OrderId(val value: String) {
    init {
        require(value.isNotBlank()) { "Order ID cannot be blank" }
        require(value.length <= 100) { "Order ID cannot exceed 100 characters" }
    }
}