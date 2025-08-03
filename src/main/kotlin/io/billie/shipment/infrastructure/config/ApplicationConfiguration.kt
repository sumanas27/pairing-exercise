package io.billie.shipment.infrastructure.config

import io.billie.shipment.domain.repository.OrderRepository
import io.billie.shipment.domain.service.EventPublisher
import io.billie.shipment.domain.service.PaymentService
import io.billie.shipment.domain.service.ShipmentService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfiguration {

    @Bean
    fun shipmentService(
        orderRepository: OrderRepository,
        eventPublisher: EventPublisher,
        paymentService: PaymentService
    ): ShipmentService {
        return ShipmentService(orderRepository, eventPublisher, paymentService)
    }
}