package io.billie.shipment.domain.events

import io.billie.shipment.domain.model.valueobjects.MerchantId
import io.billie.shipment.domain.model.valueobjects.Money
import io.billie.shipment.domain.model.valueobjects.OrderId
import io.billie.shipment.domain.model.valueobjects.ShipmentId
import java.time.Instant

data class ShipmentCreatedEvent(
    override val aggregateId: String,
    val shipmentId: ShipmentId,
    val orderId: OrderId,
    val merchantId: MerchantId,
    val amount: Money,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent()
