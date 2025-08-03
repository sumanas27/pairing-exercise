package io.billie.shipment.domain.model.valueobjects

data class MerchantId(val value: String) {
    init {
        require(value.isNotBlank()) { "Merchant ID cannot be blank" }
        require(value.length <= 100) { "Merchant ID cannot exceed 100 characters" }
    }
}