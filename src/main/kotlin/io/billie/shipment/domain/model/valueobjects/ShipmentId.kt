package io.billie.shipment.domain.model.valueobjects

data class ShipmentId(val value: String) {
    init {
        require(value.isNotBlank()) { "Shipment ID cannot be blank" }
        require(value.length <= 100) { "Shipment ID cannot exceed 100 characters" }
    }
}