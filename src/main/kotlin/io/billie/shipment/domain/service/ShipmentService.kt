package io.billie.shipment.domain.service

import io.billie.shipment.application.command.CreateShipmentCommand
import io.billie.shipment.application.result.ShipmentResult
import io.billie.shipment.domain.events.OrderFullyShippedEvent
import io.billie.shipment.domain.events.ShipmentCreatedEvent
import io.billie.shipment.domain.exceptions.InsufficientOrderAmountException
import io.billie.shipment.domain.exceptions.OrderNotFoundException
import io.billie.shipment.domain.exceptions.UnauthorizedMerchantException
import io.billie.shipment.domain.model.Shipment
import io.billie.shipment.domain.model.valueobjects.ShipmentId
import io.billie.shipment.domain.repository.OrderRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

class ShipmentService(
    private val orderRepository: OrderRepository,
    private val eventPublisher: EventPublisher,
    private val paymentService: PaymentService
) {
    fun createShipment(command: CreateShipmentCommand): ShipmentResult {
        val order = orderRepository.findById(command.orderId)
            ?: throw OrderNotFoundException(command.orderId)

        if (order.merchantId != command.merchantId) {
            throw UnauthorizedMerchantException(command.merchantId, command.orderId)
        }

        if (!order.canAcceptShipment(command.amount)) {
            val remaining = order.getRemainingAmount()
            throw InsufficientOrderAmountException(
                "Shipment amount ${command.amount.amount} exceeds remaining order amount ${remaining.amount}"
            )
        }

        val shipmentId = ShipmentId(UUID.randomUUID().toString())
        val shipment = Shipment(
            id = shipmentId,
            orderId = command.orderId,
            amount = command.amount,
            shippedAt = Instant.now(),
            trackingNumber = command.trackingNumber
        )

        val updatedOrder = order.addShipment(shipment)
        orderRepository.save(updatedOrder)

        // Process payment to merchant
        val paymentResult = paymentService.processPayment(command.merchantId, command.amount)

        // Publish events
        eventPublisher.publish(
            ShipmentCreatedEvent(
                aggregateId = shipment.id.value,
                shipmentId = shipment.id,
                orderId = command.orderId,
                merchantId = command.merchantId,
                amount = command.amount
            )
        )

        if (updatedOrder.isFullyShipped()) {
            eventPublisher.publish(
                OrderFullyShippedEvent(
                    aggregateId = updatedOrder.id.value,
                    orderId = updatedOrder.id,
                    merchantId = updatedOrder.merchantId
                )
            )
        }

        return ShipmentResult(
            shipmentId = shipment.id,
            paymentResult = paymentResult,
            remainingAmount = updatedOrder.getRemainingAmount()
        )
    }
}