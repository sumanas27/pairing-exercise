package io.billie.shipment.infrastructure.persistence.repository

import io.billie.shipment.domain.model.Order
import io.billie.shipment.domain.model.Shipment
import io.billie.shipment.domain.model.valueobjects.MerchantId
import io.billie.shipment.domain.model.valueobjects.Money
import io.billie.shipment.domain.model.valueobjects.OrderId
import io.billie.shipment.domain.model.valueobjects.ShipmentId
import io.billie.shipment.domain.repository.OrderRepository
import io.billie.shipment.infrastructure.persistence.entity.OrderEntity
import io.billie.shipment.infrastructure.persistence.entity.ShipmentEntity
import org.springframework.stereotype.Repository

@Repository
class OrderRepositoryImpl(
    private val jpaRepository: OrderJpaRepository
) : OrderRepository {
    override fun findById(orderId: OrderId): Order? {
        return jpaRepository.findById(orderId.value)
            .map { it.toDomainModel() }
            .orElse(null)
    }

    override fun save(order: Order): Order {
        val entity = order.toEntity()
        val savedEntity = jpaRepository.save(entity)
        return savedEntity.toDomainModel()
    }
}

private fun OrderEntity.toDomainModel(): Order {
    return Order(
        id = OrderId(this.id),
        merchantId = MerchantId(this.merchantId),
        totalAmount = Money(this.totalAmount, this.currency),
        status = this.status,
        createdAt = this.createdAt,
        shipments = this.shipments.map { it.toDomainModel() }.toMutableList()
    )
}

private fun ShipmentEntity.toDomainModel(): Shipment {
    return Shipment(
        id = ShipmentId(this.id),
        orderId = OrderId(this.order.id),
        amount = Money(this.amount, this.currency),
        shippedAt = this.shippedAt,
        trackingNumber = this.trackingNumber
    )
}

private fun Order.toEntity(): OrderEntity {
    val orderEntity = OrderEntity(
        id = this.id.value,
        merchantId = this.merchantId.value,
        totalAmount = this.totalAmount.amount,
        currency = this.totalAmount.currency,
        status = this.status,
        createdAt = this.createdAt
    )

    // Add shipments
    this.shipments.forEach { shipment ->
        val shipmentEntity = ShipmentEntity(
            id = shipment.id.value,
            amount = shipment.amount.amount,
            currency = shipment.amount.currency,
            shippedAt = shipment.shippedAt,
            trackingNumber = shipment.trackingNumber,
            order = orderEntity
        )
        orderEntity.shipments.add(shipmentEntity)
    }

    return orderEntity
}