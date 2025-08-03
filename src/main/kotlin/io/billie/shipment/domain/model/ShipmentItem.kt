package io.billie.shipment.domain.model

import io.billie.shipment.domain.model.valueobjects.Money

data class ShipmentItem(
    val name: String,
    val quantity: Int,
    val unitPrice: Money
)