package io.billie.shipment.domain.service

import io.billie.shipment.domain.model.valueobjects.MerchantId
import io.billie.shipment.domain.model.valueobjects.Money

interface PaymentService {
    fun processPayment(merchantId: MerchantId, amount: Money): PaymentResult
}
