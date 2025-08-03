package io.billie.shipment.infrastructure.persistence.entity

import io.billie.shipment.domain.model.OrderStatus
import java.math.BigDecimal
import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "shipments")
class ShipmentEntity(
    @Id
    @Column(name = "id", nullable = false, length = 100)
    var id: String = "",

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    var amount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "currency", nullable = false, length = 3)
    var currency: String = "",

    @Column(name = "shipped_at", nullable = false)
    var shippedAt: Instant = Instant.now(),

    @Column(name = "tracking_number", length = 255)
    var trackingNumber: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: OrderEntity = OrderEntity()
) {
    constructor() : this(
        id = "",
        amount = BigDecimal.ZERO,
        currency = "",
        shippedAt = Instant.now(),
        trackingNumber = "",
        order = OrderEntity()
    )
}