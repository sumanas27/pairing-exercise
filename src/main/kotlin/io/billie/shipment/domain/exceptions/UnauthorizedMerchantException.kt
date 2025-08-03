package io.billie.shipment.domain.exceptions

import io.billie.shipment.domain.model.valueobjects.MerchantId
import io.billie.shipment.domain.model.valueobjects.OrderId

class UnauthorizedMerchantException(merchantId: MerchantId, orderId: OrderId) :
    RuntimeException("Merchant ${merchantId.value} not authorized for order ${orderId.value}")
