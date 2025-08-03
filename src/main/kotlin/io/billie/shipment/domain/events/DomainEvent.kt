package io.billie.shipment.domain.events

import java.time.Instant

sealed class DomainEvent {
    abstract val occurredAt: Instant
    abstract val aggregateId: String
}