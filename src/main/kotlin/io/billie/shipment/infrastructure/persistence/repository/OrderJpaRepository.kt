package io.billie.shipment.infrastructure.persistence.repository

import io.billie.shipment.infrastructure.persistence.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderJpaRepository : JpaRepository<OrderEntity, String>