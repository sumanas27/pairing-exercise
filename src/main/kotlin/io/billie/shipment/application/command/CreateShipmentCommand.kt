package io.billie.shipment.application.command

import io.billie.shipment.domain.model.valueobjects.MerchantId
import io.billie.shipment.domain.model.valueobjects.Money
import io.billie.shipment.domain.model.valueobjects.OrderId

data class CreateShipmentCommand(
    val orderId: OrderId,
    val merchantId: MerchantId,
    val amount: Money,
    val trackingNumber: String? = null
)
