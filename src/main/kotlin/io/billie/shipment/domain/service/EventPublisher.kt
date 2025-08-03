package io.billie.shipment.domain.service

import io.billie.shipment.domain.events.DomainEvent

interface EventPublisher {
    fun publish(event: DomainEvent)
}
