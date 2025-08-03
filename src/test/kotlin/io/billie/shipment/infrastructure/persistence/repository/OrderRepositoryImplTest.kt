package io.billie.shipment.infrastructure.persistence.repository

import io.billie.shipment.domain.model.Order
import io.billie.shipment.domain.model.OrderStatus
import io.billie.shipment.domain.model.Shipment
import io.billie.shipment.domain.model.valueobjects.MerchantId
import io.billie.shipment.domain.model.valueobjects.Money
import io.billie.shipment.domain.model.valueobjects.OrderId
import io.billie.shipment.domain.model.valueobjects.ShipmentId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.math.BigDecimal
import java.time.Instant

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("JPA Order Repository")
class OrderRepositoryImplTest {

    @Autowired
    private lateinit var orderRepository: OrderRepositoryImpl

    @Nested
    @DisplayName("Order Persistence")
    inner class OrderPersistence {

        @Test
        fun `should save and retrieve order successfully`() {
            // Given
            val order = Order(
                id = OrderId("ORDER-123"),
                merchantId = MerchantId("MERCHANT-456"),
                totalAmount = Money(BigDecimal("100.00"), "EUR"),
                status = OrderStatus.PENDING,
                createdAt = Instant.now()
            )

            // When
            val savedOrder = orderRepository.save(order)
            val retrievedOrder = orderRepository.findById(order.id)

            // Then
            assertNotNull(retrievedOrder)
            assertEquals(order.id, retrievedOrder!!.id)
            assertEquals(order.merchantId, retrievedOrder.merchantId)
            assertEquals(order.totalAmount, retrievedOrder.totalAmount)
            assertEquals(order.status, retrievedOrder.status)
        }

        @Test
        fun `should return null when order not found`() {
            // Given
            val nonExistentOrderId = OrderId("NON-EXISTENT")

            // When
            val result = orderRepository.findById(nonExistentOrderId)

            // Then
            assertNull(result)
        }

        @Test
        fun `should save order with shipments`() {
            // Given
            val shipment = Shipment(
                id = ShipmentId("SHIP-123"),
                orderId = OrderId("ORDER-123"),
                amount = Money(BigDecimal("50.00"), "EUR"),
                shippedAt = Instant.now(),
                trackingNumber = "TRACK-456"
            )

            val order = Order(
                id = OrderId("ORDER-123"),
                merchantId = MerchantId("MERCHANT-456"),
                totalAmount = Money(BigDecimal("100.00"), "EUR"),
                status = OrderStatus.PARTIALLY_SHIPPED,
                createdAt = Instant.now(),
                shipments = mutableListOf(shipment)
            )

            // When
            val savedOrder = orderRepository.save(order)
            val retrievedOrder = orderRepository.findById(order.id)

            // Then
            assertNotNull(retrievedOrder)
            assertEquals(1, retrievedOrder!!.shipments.size)
            assertEquals(shipment.id, retrievedOrder.shipments.first().id)
            assertEquals(shipment.amount, retrievedOrder.shipments.first().amount)
            assertEquals(shipment.trackingNumber, retrievedOrder.shipments.first().trackingNumber)
        }
    }
}