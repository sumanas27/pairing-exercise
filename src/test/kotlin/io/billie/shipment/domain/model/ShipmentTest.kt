package io.billie.shipment.domain.model

import io.billie.shipment.domain.model.valueobjects.Money
import io.billie.shipment.domain.model.valueobjects.OrderId
import io.billie.shipment.domain.model.valueobjects.ShipmentId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant

@DisplayName("Shipment Domain Entity")
class ShipmentTest {

    @Test
    fun `should create shipment with valid parameters`() {
        val shipmentId = ShipmentId("SHIP-123")
        val orderId = OrderId("ORDER-456")
        val amount = Money(BigDecimal("50.00"), "EUR")
        val shippedAt = Instant.now()
        val trackingNumber = "TRACK-789"

        val shipment = Shipment(shipmentId, orderId, amount, shippedAt, trackingNumber)

        assertEquals(shipmentId, shipment.id)
        assertEquals(orderId, shipment.orderId)
        assertEquals(amount, shipment.amount)
        assertEquals(shippedAt, shipment.shippedAt)
        assertEquals(trackingNumber, shipment.trackingNumber)
    }

    @Test
    fun `should reject shipment with zero amount`() {
        assertThrows<IllegalArgumentException> {
            Shipment(
                ShipmentId("SHIP-123"),
                OrderId("ORDER-456"),
                Money(BigDecimal.ZERO, "EUR"),
                Instant.now()
            )
        }
    }

    @Test
    fun `should reject shipment with negative amount`() {
        assertThrows<IllegalArgumentException> {
            Shipment(
                ShipmentId("SHIP-123"),
                OrderId("ORDER-456"),
                Money(BigDecimal("-10.00"), "EUR"),
                Instant.now()
            )
        }
    }
}