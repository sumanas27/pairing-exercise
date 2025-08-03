package io.billie.shipment.domain.service

import io.billie.shipment.application.command.CreateShipmentCommand
import io.billie.shipment.domain.events.OrderFullyShippedEvent
import io.billie.shipment.domain.events.ShipmentCreatedEvent
import io.billie.shipment.domain.exceptions.InsufficientOrderAmountException
import io.billie.shipment.domain.exceptions.OrderNotFoundException
import io.billie.shipment.domain.exceptions.UnauthorizedMerchantException
import io.billie.shipment.domain.model.Order
import io.billie.shipment.domain.model.OrderStatus
import io.billie.shipment.domain.model.Shipment
import io.billie.shipment.domain.model.valueobjects.MerchantId
import io.billie.shipment.domain.model.valueobjects.Money
import io.billie.shipment.domain.model.valueobjects.OrderId
import io.billie.shipment.domain.model.valueobjects.ShipmentId
import io.billie.shipment.domain.repository.OrderRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant

@DisplayName("Shipment Service")
class ShipmentServiceTest {

    private lateinit var orderRepository: OrderRepository
    private lateinit var eventPublisher: EventPublisher
    private lateinit var paymentService: PaymentService
    private lateinit var shipmentService: ShipmentService

    private val orderId = OrderId("ORDER-123")
    private val merchantId = MerchantId("MERCHANT-456")
    private val differentMerchantId = MerchantId("DIFFERENT-MERCHANT")
    private val orderAmount = Money(BigDecimal("100.00"), "EUR")

    @BeforeEach
    fun setup() {
        orderRepository = mock()
        eventPublisher = mock()
        paymentService = mock()
        shipmentService = ShipmentService(orderRepository, eventPublisher, paymentService)
    }

    @Nested
    @DisplayName("Successful Shipment Creation")
    inner class SuccessfulShipmentCreation {

        @Test
        fun `should create shipment successfully when all conditions are met`() {
            // Given
            val shipmentAmount = Money(BigDecimal("50.00"), "EUR")
            val order = createOrder()
            val command = CreateShipmentCommand(orderId, merchantId, shipmentAmount)
            val paymentResult = PaymentResult(true, "TXN-123")

            whenever(orderRepository.findById(orderId)).thenReturn(order)
            whenever(orderRepository.save(order)).thenReturn(order)
            whenever(paymentService.processPayment(merchantId, shipmentAmount)).thenReturn(paymentResult)

            // When
            val result = shipmentService.createShipment(command)

            // Then
            assertTrue(result.paymentResult.success)
            assertEquals("TXN-123", result.paymentResult.transactionId)
            assertEquals(BigDecimal("50.00"), result.remainingAmount.amount)
            assertEquals("EUR", result.remainingAmount.currency)
            assertNotNull(result.shipmentId)

            verify(orderRepository).findById(orderId)
            verify(orderRepository).save(any<Order>())
            verify(paymentService).processPayment(merchantId, shipmentAmount)
            verify(eventPublisher).publish(any<ShipmentCreatedEvent>())
            verify(eventPublisher, never()).publish(any<OrderFullyShippedEvent>())
        }

        @Test
        fun `should publish OrderFullyShippedEvent when order is completely shipped`() {
            // Given
            val shipmentAmount = Money(BigDecimal("100.00"), "EUR") // Full order amount
            val order = createOrder()
            val command = CreateShipmentCommand(orderId, merchantId, shipmentAmount)
            val paymentResult = PaymentResult(true, "TXN-123")

            whenever(orderRepository.findById(orderId)).thenReturn(order)
            whenever(orderRepository.save(any())).thenReturn(order)
            whenever(paymentService.processPayment(merchantId, shipmentAmount)).thenReturn(paymentResult)

            // When
            val result = shipmentService.createShipment(command)

            // Then
            assertTrue(result.remainingAmount.isZero())

            verify(eventPublisher).publish(any<ShipmentCreatedEvent>())
            verify(eventPublisher).publish(any<OrderFullyShippedEvent>())
        }

        @Test
        fun `should handle partial shipments correctly`() {
            // Given
            val firstShipmentAmount = Money(BigDecimal("30.00"), "EUR")
            val secondShipmentAmount = Money(BigDecimal("20.00"), "EUR")
            val order = createOrder()

            // First shipment
            val firstCommand = CreateShipmentCommand(orderId, merchantId, firstShipmentAmount)
            val paymentResult = PaymentResult(true, "TXN-123")

            whenever(orderRepository.findById(orderId)).thenReturn(order)
            whenever(orderRepository.save(any())).thenReturn(order)
            whenever(paymentService.processPayment(merchantId, firstShipmentAmount)).thenReturn(paymentResult)

            // When
            val firstResult = shipmentService.createShipment(firstCommand)

            // Then
            assertEquals(BigDecimal("70.00"), firstResult.remainingAmount.amount)
            verify(eventPublisher, times(1)).publish(any<ShipmentCreatedEvent>())
            verify(eventPublisher, never()).publish(any<OrderFullyShippedEvent>())
        }
    }

    @Nested
    @DisplayName("Error Scenarios")
    inner class ErrorScenarios {

        @Test
        fun `should throw OrderNotFoundException when order does not exist`() {
            // Given
            val command = CreateShipmentCommand(
                orderId, merchantId, Money(BigDecimal("50.00"), "EUR")
            )
            whenever(orderRepository.findById(orderId)).thenReturn(null)

            // When & Then
            val exception = assertThrows<OrderNotFoundException> {
                shipmentService.createShipment(command)
            }

            assertEquals("Order not found: ${orderId.value}", exception.message)
            verify(orderRepository).findById(orderId)
            verify(orderRepository, never()).save(any())
            verify(paymentService, never()).processPayment(any(), any())
            verify(eventPublisher, never()).publish(any())
        }

        @Test
        fun `should throw UnauthorizedMerchantException when merchant does not own order`() {
            // Given
            val order = createOrder()
            val command = CreateShipmentCommand(
                orderId, differentMerchantId, Money(BigDecimal("50.00"), "EUR")
            )
            whenever(orderRepository.findById(orderId)).thenReturn(order)

            // When & Then
            val exception = assertThrows<UnauthorizedMerchantException> {
                shipmentService.createShipment(command)
            }

            assertTrue(exception.message!!.contains("DIFFERENT-MERCHANT"))
            assertTrue(exception.message!!.contains("not authorized"))
            verify(orderRepository, never()).save(any())
            verify(paymentService, never()).processPayment(any(), any())
            verify(eventPublisher, never()).publish(any())
        }

        @Test
        fun `should throw InsufficientOrderAmountException when shipment exceeds order total`() {
            // Given
            val excessiveAmount = Money(BigDecimal("150.00"), "EUR")
            val order = createOrder()
            val command = CreateShipmentCommand(orderId, merchantId, excessiveAmount)
            whenever(orderRepository.findById(orderId)).thenReturn(order)

            // When & Then
            val exception = assertThrows<InsufficientOrderAmountException> {
                shipmentService.createShipment(command)
            }

            assertTrue(exception.message!!.contains("exceeds remaining order amount"))
            verify(orderRepository, never()).save(any())
            verify(paymentService, never()).processPayment(any(), any())
            verify(eventPublisher, never()).publish(any())
        }

        @Test
        fun `should throw InsufficientOrderAmountException when partial shipments exceed total`() {
            // Given
            val existingShipment = Shipment(
                ShipmentId("EXISTING-SHIP"),
                orderId,
                Money(BigDecimal("80.00"), "EUR"),
                Instant.now()
            )
            val orderWithExistingShipment = createOrder(shipments = mutableListOf(existingShipment))
            val excessiveAmount = Money(BigDecimal("30.00"), "EUR") // 80 + 30 = 110 > 100
            val command = CreateShipmentCommand(orderId, merchantId, excessiveAmount)

            whenever(orderRepository.findById(orderId)).thenReturn(orderWithExistingShipment)

            // When & Then
            assertThrows<InsufficientOrderAmountException> {
                shipmentService.createShipment(command)
            }
        }
    }

    @Nested
    @DisplayName("Payment Integration")
    inner class PaymentIntegration {

        @Test
        fun `should handle payment failure gracefully`() {
            // Given
            val shipmentAmount = Money(BigDecimal("50.00"), "EUR")
            val order = createOrder()
            val command = CreateShipmentCommand(orderId, merchantId, shipmentAmount)
            val failedPaymentResult = PaymentResult(false, null, "Payment failed")

            whenever(orderRepository.findById(orderId)).thenReturn(order)
            whenever(orderRepository.save(any())).thenReturn(order)
            whenever(paymentService.processPayment(merchantId, shipmentAmount)).thenReturn(failedPaymentResult)

            // When
            val result = shipmentService.createShipment(command)

            // Then
            assertFalse(result.paymentResult.success)
            assertEquals("Payment failed", result.paymentResult.errorMessage)
            assertNull(result.paymentResult.transactionId)

            // Shipment should still be created and events published even if payment fails
            verify(orderRepository).save(any())
            verify(eventPublisher).publish(any<ShipmentCreatedEvent>())
        }
    }

    private fun createOrder(
        id: OrderId = orderId,
        merchant: MerchantId = merchantId,
        amount: Money = orderAmount,
        status: OrderStatus = OrderStatus.PENDING,
        shipments: MutableList<Shipment> = mutableListOf()
    ) = Order(id, merchant, amount, status, Instant.now(), shipments)
}