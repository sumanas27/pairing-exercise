package io.billie.shipment.infrastructure.payment

import io.billie.shipment.domain.model.valueobjects.MerchantId
import io.billie.shipment.domain.model.valueobjects.Money
import io.billie.shipment.domain.service.PaymentResult
import io.billie.shipment.domain.service.PaymentService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Random
import java.util.UUID

@Service
class MockPaymentService(
    @Value("\${payment.mock.failure-rate:0.1}") private val failureRate: Double = 0.1
) : PaymentService {

    private val logger = LoggerFactory.getLogger(MockPaymentService::class.java)
    private val random = Random()

    override fun processPayment(merchantId: MerchantId, amount: Money): PaymentResult {
        logger.info("Processing payment for merchant: ${merchantId.value}, amount: ${amount.amount} ${amount.currency}")

        // Simulate processing time
        Thread.sleep(100)

        // Simulate random failures based on configured failure rate
        val shouldFail = random.nextDouble() < failureRate

        return if (shouldFail) {
            logger.warn("Payment failed for merchant: ${merchantId.value}")
            PaymentResult(
                success = false,
                transactionId = null,
                errorMessage = "Payment processing failed - insufficient funds or invalid payment method"
            )
        } else {
            val transactionId = "TXN-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
            logger.info("Payment successful for merchant: ${merchantId.value}, transaction: $transactionId")
            PaymentResult(
                success = true,
                transactionId = transactionId,
                errorMessage = null
            )
        }
    }
}