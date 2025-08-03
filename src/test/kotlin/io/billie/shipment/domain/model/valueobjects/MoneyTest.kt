package io.billie.shipment.domain.model.valueobjects

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

@DisplayName("Money Value Object")
class MoneyTest {

    @Test
    fun `should create money with valid amount and currency`() {
        val money = Money(BigDecimal("100.50"), "EUR")

        assertEquals(BigDecimal("100.50"), money.amount)
        assertEquals("EUR", money.currency)
    }

    @Test
    fun `should reject negative amount`() {
        assertThrows<IllegalArgumentException> {
            Money(BigDecimal("-10.00"), "EUR")
        }
    }

    @Test
    fun `should reject invalid currency format`() {
        assertThrows<IllegalArgumentException> {
            Money(BigDecimal("100.00"), "EURO")
        }

        assertThrows<IllegalArgumentException> {
            Money(BigDecimal("100.00"), "")
        }
    }

    @Test
    fun `should add money with same currency`() {
        val money1 = Money(BigDecimal("50.00"), "EUR")
        val money2 = Money(BigDecimal("30.00"), "EUR")

        val result = money1 + money2

        assertEquals(BigDecimal("80.00"), result.amount)
        assertEquals("EUR", result.currency)
    }

    @Test
    fun `should not add money with different currencies`() {
        val money1 = Money(BigDecimal("50.00"), "EUR")
        val money2 = Money(BigDecimal("30.00"), "USD")

        assertThrows<IllegalArgumentException> {
            money1 + money2
        }
    }

    @Test
    fun `should subtract money correctly`() {
        val money1 = Money(BigDecimal("100.00"), "EUR")
        val money2 = Money(BigDecimal("30.00"), "EUR")

        val result = money1 - money2

        assertEquals(BigDecimal("70.00"), result.amount)
    }

    @Test
    fun `should not allow negative subtraction result`() {
        val money1 = Money(BigDecimal("30.00"), "EUR")
        val money2 = Money(BigDecimal("100.00"), "EUR")

        assertThrows<IllegalArgumentException> {
            money1 - money2
        }
    }

    @Test
    fun `should compare money correctly`() {
        val money1 = Money(BigDecimal("100.00"), "EUR")
        val money2 = Money(BigDecimal("50.00"), "EUR")
        val money3 = Money(BigDecimal("100.00"), "EUR")

        assertTrue(money1 > money2)
        assertTrue(money2 < money1)
        assertEquals(0, money1.compareTo(money3))
    }

    @Test
    fun `should identify zero amount`() {
        val zeroMoney = Money(BigDecimal.ZERO, "EUR")
        val nonZeroMoney = Money(BigDecimal("10.00"), "EUR")

        assertTrue(zeroMoney.isZero())
        assertFalse(nonZeroMoney.isZero())
    }
}