package io.billie.shipment.domain.model

import io.billie.shipment.domain.model.valueobjects.MerchantId
import io.billie.shipment.domain.model.valueobjects.Money
import io.billie.shipment.domain.model.valueobjects.OrderId
import io.billie.shipment.domain.model.valueobjects.ShipmentId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant

@DisplayName("Order Domain Entity")
class OrderTest {

    private val orderId = OrderId("ORDER-123")
    private val merchantId = MerchantId("MERCHANT-456")
    private val totalAmount = Money(BigDecimal("100.00"), "EUR")
    private val createdAt = Instant.now()

    private fun createOrder(
        id: OrderId = orderId,
        merchant: MerchantId = merchantId,
        amount: Money = totalAmount,
        status: OrderStatus = OrderStatus.PENDING,
        shipments: MutableList<Shipment> = mutableListOf()
    ) = Order(id, merchant, amount, status, createdAt, shipments)

    @Nested
    @DisplayName("Order Creation")
    inner class OrderCreation {

        @Test
        fun `should create order with valid parameters`() {
            val order = createOrder()

            assertEquals(orderId, order.id)
            assertEquals(merchantId, order.merchantId)
            assertEquals(totalAmount, order.totalAmount)
            assertEquals(OrderStatus.PENDING, order.status)
            assertTrue(order.shipments.isEmpty())
        }
    }

    @Nested
    @DisplayName("Shipment Amount Calculations")
    inner class ShipmentAmountCalculations {

        @Test
        fun `should return zero shipped amount for order with no shipments`() {
            val order = createOrder()

            val shippedAmount = order.getTotalShippedAmount()

            assertTrue(shippedAmount.isZero())
            assertEquals("EUR", shippedAmount.currency)
        }

        @Test
        fun `should calculate total shipped amount correctly`() {
            val shipment1 = createShipment(amount = Money(BigDecimal("30.00"), "EUR"))
            val shipment2 = createShipment(
                id = ShipmentId("SHIP-2"),
                amount = Money(BigDecimal("20.00"), "EUR")
            )
            val order = createOrder(shipments = mutableListOf(shipment1, shipment2))

            val shippedAmount = order.getTotalShippedAmount()

            assertEquals(BigDecimal("50.00"), shippedAmount.amount)
        }

        @Test
        fun `should calculate remaining amount correctly`() {
            val shipment = createShipment(amount = Money(BigDecimal("30.00"), "EUR"))
            val order = createOrder(shipments = mutableListOf(shipment))

            val remainingAmount = order.getRemainingAmount()

            assertEquals(BigDecimal("70.00"), remainingAmount.amount)
        }
    }

    @Nested
    @DisplayName("Shipment Validation")
    inner class ShipmentValidation {

        @Test
        fun `should accept shipment when amount is within remaining total`() {
            val order = createOrder()
            val shipmentAmount = Money(BigDecimal("50.00"), "EUR")

            assertTrue(order.canAcceptShipment(shipmentAmount))
        }

        @Test
        fun `should reject shipment when amount exceeds remaining total`() {
            val order = createOrder()
            val shipmentAmount = Money(BigDecimal("150.00"), "EUR")

            assertFalse(order.canAcceptShipment(shipmentAmount))
        }

        @Test
        fun `should reject shipment with different currency`() {
            val order = createOrder()
            val shipmentAmount = Money(BigDecimal("50.00"), "USD")

            assertThrows<IllegalArgumentException> {
                order.canAcceptShipment(shipmentAmount)
            }
        }

        @Test
        fun `should accept shipment for exact remaining amount`() {
            val existingShipment = createShipment(amount = Money(BigDecimal("30.00"), "EUR"))
            val order = createOrder(shipments = mutableListOf(existingShipment))
            val shipmentAmount = Money(BigDecimal("70.00"), "EUR")

            assertTrue(order.canAcceptShipment(shipmentAmount))
        }
    }

    @Nested
    @DisplayName("Adding Shipments")
    inner class AddingShipments {

        @Test
        fun `should add shipment successfully`() {
            val order = createOrder()
            val shipment = createShipment(amount = Money(BigDecimal("50.00"), "EUR"))

            val updatedOrder = order.addShipment(shipment)

            assertEquals(1, updatedOrder.shipments.size)
            assertEquals(shipment, updatedOrder.shipments.first())
            assertEquals(OrderStatus.PARTIALLY_SHIPPED, updatedOrder.status)
        }

        @Test
        fun `should update status to fully shipped when order complete`() {
            val order = createOrder()
            val shipment = createShipment(amount = Money(BigDecimal("100.00"), "EUR"))

            val updatedOrder = order.addShipment(shipment)

            assertEquals(OrderStatus.FULLY_SHIPPED, updatedOrder.status)
            assertTrue(updatedOrder.isFullyShipped())
        }

        @Test
        fun `should reject shipment that exceeds order total`() {
            val order = createOrder()
            val shipment = createShipment(amount = Money(BigDecimal("150.00"), "EUR"))

            assertThrows<IllegalArgumentException> {
                order.addShipment(shipment)
            }
        }

        @Test
        fun `should reject shipment with mismatched order ID`() {
            val order = createOrder()
            val shipment = createShipment(
                orderId = OrderId("DIFFERENT-ORDER"),
                amount = Money(BigDecimal("50.00"), "EUR")
            )

            assertThrows<IllegalArgumentException> {
                order.addShipment(shipment)
            }
        }
    }

    private fun createShipment(
        id: ShipmentId = ShipmentId("SHIP-1"),
        orderId: OrderId = this.orderId,
        amount: Money,
        shippedAt: Instant = Instant.now()
    ) = Shipment(id, orderId, amount, shippedAt)
}