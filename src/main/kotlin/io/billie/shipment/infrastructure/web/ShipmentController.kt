package io.billie.shipment.infrastructure.web

import io.billie.shipment.application.command.CreateShipmentCommand
import io.billie.shipment.application.dto.CreateShipmentRequest
import io.billie.shipment.application.dto.CreateShipmentResponse
import io.billie.shipment.domain.exceptions.InsufficientOrderAmountException
import io.billie.shipment.domain.exceptions.OrderNotFoundException
import io.billie.shipment.domain.exceptions.UnauthorizedMerchantException
import io.billie.shipment.domain.model.valueobjects.MerchantId
import io.billie.shipment.domain.model.valueobjects.Money
import io.billie.shipment.domain.model.valueobjects.OrderId
import io.billie.shipment.domain.service.ShipmentService
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/orders/{orderId}/shipments")
@Validated
class ShipmentController(
    private val shipmentService: ShipmentService
) {

    @PostMapping
    fun createShipment(
        @PathVariable merchantId: String,
        @PathVariable orderId: String,
        @Valid @RequestBody request: CreateShipmentRequest
    ): ResponseEntity<CreateShipmentResponse> {
        return try {
            val command = CreateShipmentCommand(
                orderId = OrderId(orderId),
                merchantId = MerchantId(merchantId),
                amount = Money(request.amount, request.currency),
                trackingNumber = request.trackingNumber
            )

            val result = shipmentService.createShipment(command)

            val response = CreateShipmentResponse(
                success = true,
                shipmentId = result.shipmentId.value,
                transactionId = result.paymentResult.transactionId,
                remainingAmount = result.remainingAmount.amount,
                currency = result.remainingAmount.currency,
                errorMessage = result.paymentResult.errorMessage
            )

            ResponseEntity.ok(response)

        } catch (e: OrderNotFoundException) {
            ResponseEntity.status(NOT_FOUND)
                .body(CreateShipmentResponse(
                    success = false,
                    errorMessage = e.message ?: "Order not found"
                ))

        } catch (e: UnauthorizedMerchantException) {
            ResponseEntity.status(FORBIDDEN)
                .body(CreateShipmentResponse(
                    success = false,
                    errorMessage = e.message ?: "Unauthorized merchant"
                ))

        } catch (e: InsufficientOrderAmountException) {
            ResponseEntity.status(BAD_REQUEST)
                .body(CreateShipmentResponse(
                    success = false,
                    errorMessage = e.message ?: "Insufficient order amount"
                ))

        } catch (e: Exception) {
            ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(CreateShipmentResponse(
                    success = false,
                    errorMessage = "Internal server error"
                ))
        }
    }
}