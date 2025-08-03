package io.billie.shipment.domain.model.valueobjects

import java.math.BigDecimal

data class Money(val amount: BigDecimal, val currency: String) {
    init {
        require(amount >= BigDecimal.ZERO) { "Amount cannot be negative" }
        require(currency.isNotBlank()) { "Currency cannot be blank" }
        require(currency.length == 3) { "Currency must be 3 characters (ISO 4217)" }
    }

    operator fun plus(other: Money): Money {
        require(this.currency == other.currency) { "Cannot add different currencies" }
        return Money(this.amount + other.amount, this.currency)
    }

    operator fun minus(other: Money): Money {
        require(this.currency == other.currency) { "Cannot subtract different currencies" }
        require(this.amount >= other.amount) { "Cannot have negative result" }
        return Money(this.amount - other.amount, this.currency)
    }

    operator fun compareTo(other: Money): Int {
        require(this.currency == other.currency) { "Cannot compare different currencies" }
        return this.amount.compareTo(other.amount)
    }

    fun isZero(): Boolean = amount.compareTo(BigDecimal.ZERO) == 0

    fun isGreaterThanZero(): Boolean = amount >= BigDecimal.ZERO
}