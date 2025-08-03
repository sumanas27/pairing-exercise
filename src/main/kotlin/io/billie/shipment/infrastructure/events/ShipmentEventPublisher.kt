package io.billie.shipment.infrastructure.events

import com.fasterxml.jackson.databind.ObjectMapper
import io.billie.shipment.domain.events.DomainEvent
import io.billie.shipment.domain.service.EventPublisher
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class ShipmentEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper
) : EventPublisher {

    private val logger = LoggerFactory.getLogger(ShipmentEventPublisher::class.java)

    override fun publish(event: DomainEvent) {
        try {
            logger.info("Publishing domain event: ${event::class.simpleName} for aggregate: ${event.aggregateId}")

            // Publish to Spring's event system
            applicationEventPublisher.publishEvent(event)

            // Log event details for monitoring/debugging
            val eventJson = objectMapper.writeValueAsString(event)
            logger.debug("Event details: $eventJson")

        } catch (e: Exception) {
            logger.error("Failed to publish domain event: ${event::class.simpleName}", e)
            // Don't rethrow - event publishing failures shouldn't break the main flow
        }
    }
}