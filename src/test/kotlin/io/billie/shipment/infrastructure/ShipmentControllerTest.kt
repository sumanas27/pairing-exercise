package io.billie.shipment.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import io.billie.shipment.application.dto.CreateShipmentRequest
import io.billie.shipment.application.result.ShipmentResult
import io.billie.shipment.domain.exceptions.InsufficientOrderAmountException
import io.billie.shipment.domain.exceptions.OrderNotFoundException
import io.billie.shipment.domain.exceptions.UnauthorizedMerchantException
import io.billie.shipment.domain.model.valueobjects.MerchantId
import io.billie.shipment.domain.model.valueobjects.Money
import io.billie.shipment.domain.model.valueobjects.OrderId
import io.billie.shipment.domain.model.valueobjects.ShipmentId
import io.billie.shipment.domain.service.PaymentResult
import io.billie.shipment.domain.service.ShipmentService
import io.billie.shipment.infrastructure.persistence.repository.OrderJpaRepository
import io.billie.shipment.infrastructure.web.ShipmentController
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

@WebMvcTest(ShipmentController::class)
@DisplayName("Shipment Controller")
class ShipmentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var shipmentService: ShipmentService

    @MockBean
    private lateinit var orderJpaRepository: OrderJpaRepository

    private val merchantId = "MERCHANT-123"
    private val orderId = "ORDER-456"
    private val baseUrl = "/api/v1/merchants/$merchantId/orders/$orderId/shipments"

    @Nested
    @DisplayName("Successful Requests")
    inner class SuccessfulRequests {

        @Test
        fun `should create shipment successfully`() {
            // Given
            val request = CreateShipmentRequest(
                orderId = orderId,
                amount = BigDecimal("50.00"),
                currency = "EUR",
                trackingNumber = "TRACK-123"
            )

            val shipmentResult = ShipmentResult(
                shipmentId = ShipmentId("SHIP-789"),
                paymentResult = PaymentResult(true, "TXN-456"),
                remainingAmount = Money(BigDecimal("50.00"), "EUR")
            )

            whenever(shipmentService.createShipment(any())).thenReturn(shipmentResult)

            // When & Then
            mockMvc.perform(
                post(baseUrl)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.shipmentId").value("SHIP-789"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.transactionId").value("TXN-456"))
                .andExpect(jsonPath("$.remainingAmount").value(50.00))
                .andExpect(jsonPath("$.currency").value("EUR"))

            verify(shipmentService).createShipment(argThat { command ->
                command.orderId.value == orderId &&
                        command.merchantId.value == merchantId &&
                        command.amount.amount == BigDecimal("50.00") &&
                        command.amount.currency == "EUR" &&
                        command.trackingNumber == "TRACK-123"
            })
        }

        @Test
        fun `should create shipment without tracking number`() {
            // Given
            val request = CreateShipmentRequest(
                orderId = orderId,
                amount = BigDecimal("50.00"),
                currency = "EUR"
            )

            val shipmentResult = ShipmentResult(
                shipmentId = ShipmentId("SHIP-789"),
                paymentResult = PaymentResult(true, "TXN-456"),
                remainingAmount = Money(BigDecimal("50.00"), "EUR")
            )

            whenever(shipmentService.createShipment(any())).thenReturn(shipmentResult)

            // When & Then
            mockMvc.perform(
                post(baseUrl)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))

            verify(shipmentService).createShipment(argThat { command ->
                command.trackingNumber == null
            })
        }
    }

    @Nested
    @DisplayName("Error Handling")
    inner class ErrorHandling {

        @Test
        fun `should return 404 when order not found`() {
            // Given
            val request = CreateShipmentRequest(
                orderId = orderId,
                amount = BigDecimal("50.00"),
                currency = "EUR"
            )

            whenever(shipmentService.createShipment(any()))
                .thenThrow(OrderNotFoundException(OrderId(orderId)))

            // When & Then
            mockMvc.perform(
                post(baseUrl)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("Order not found: $orderId"))
        }

        @Test
        fun `should return 403 when merchant unauthorized`() {
            // Given
            val request = CreateShipmentRequest(
                orderId = orderId,
                amount = BigDecimal("50.00"),
                currency = "EUR"
            )

            whenever(shipmentService.createShipment(any()))
                .thenThrow(UnauthorizedMerchantException(MerchantId(merchantId), OrderId(orderId)))

            // When & Then
            mockMvc.perform(
                post(baseUrl)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("Merchant $merchantId not authorized for order $orderId"))
        }

        @Test
        fun `should return 400 when shipment amount exceeds order total`() {
            // Given
            val request = CreateShipmentRequest(
                orderId = orderId,
                amount = BigDecimal("150.00"),
                currency = "EUR"
            )

            whenever(shipmentService.createShipment(any()))
                .thenThrow(InsufficientOrderAmountException("Shipment amount exceeds remaining order amount"))

            // When & Then
            mockMvc.perform(
                post(baseUrl)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("Shipment amount exceeds remaining order amount"))
        }

        @Test
        fun `should return 500 for unexpected errors`() {
            // Given
            val request = CreateShipmentRequest(
                orderId = orderId,
                amount = BigDecimal("50.00"),
                currency = "EUR"
            )

            whenever(shipmentService.createShipment(any()))
                .thenThrow(RuntimeException("Database connection failed"))

            // When & Then
            mockMvc.perform(
                post(baseUrl)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isInternalServerError)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("Internal server error"))
        }
    }

    @Nested
    @DisplayName("Input Validation")
    inner class InputValidation {

        @Test
        fun `should reject request with negative amount`() {
            // Given
            val request = CreateShipmentRequest(
                orderId = orderId,
                amount = BigDecimal("-50.00"),
                currency = "EUR"
            )

            // When & Then
            mockMvc.perform(
                post(baseUrl)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should reject request with invalid currency`() {
            // Given
            val request = CreateShipmentRequest(
                orderId = orderId,
                amount = BigDecimal("50.00"),
                currency = "INVALID"
            )

            // When & Then
            mockMvc.perform(
                post(baseUrl)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should reject request with missing required fields`() {
            // Given
            val incompleteRequest = """{"orderId": "$orderId"}"""

            // When & Then
            mockMvc.perform(
                post(baseUrl)
                    .contentType(APPLICATION_JSON)
                    .content(incompleteRequest)
            )
                .andExpect(status().isInternalServerError)
        }
    }
}