package io.billie.shipment.infrastructure.persistence.entity

import io.billie.shipment.domain.model.OrderStatus
import java.math.BigDecimal
import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "orders")
class OrderEntity(

    @Id
    @Column(name = "id", nullable = false, length = 100)
    var id: String = "",

    @Column(name = "merchant_id", nullable = false, length = 100)
    var merchantId: String = "",

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    var totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "currency", nullable = false, length = 3)
    var currency: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: OrderStatus = OrderStatus.PENDING,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    var shipments: MutableList<ShipmentEntity> = mutableListOf()

) {
    constructor() : this(
        id = "",
        merchantId = "",
        totalAmount = BigDecimal.ZERO,
        currency = "",
        status = OrderStatus.PENDING,
        createdAt = Instant.now(),
        shipments = mutableListOf()
    )
}