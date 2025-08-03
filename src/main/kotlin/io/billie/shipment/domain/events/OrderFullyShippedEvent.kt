package io.billie.shipment.domain.events

import io.billie.shipment.domain.model.valueobjects.MerchantId
import io.billie.shipment.domain.model.valueobjects.OrderId
import java.time.Instant

data class OrderFullyShippedEvent(
    override val aggregateId: String,
    val orderId: OrderId,
    val merchantId: MerchantId,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent()