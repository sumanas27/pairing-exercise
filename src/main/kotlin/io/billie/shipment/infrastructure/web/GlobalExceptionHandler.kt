package io.billie.shipment.infrastructure.web

import io.billie.shipment.application.dto.CreateShipmentResponse
import io.billie.shipment.domain.exceptions.InsufficientOrderAmountException
import io.billie.shipment.domain.exceptions.OrderNotFoundException
import io.billie.shipment.domain.exceptions.UnauthorizedMerchantException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(OrderNotFoundException::class)
    fun handleOrderNotFound(e: OrderNotFoundException): ResponseEntity<CreateShipmentResponse> {
        logger.warn("Order not found: ${e.message}")
        return ResponseEntity.status(NOT_FOUND)
            .body(CreateShipmentResponse(
                success = false,
                errorMessage = e.message
            ))
    }

    @ExceptionHandler(UnauthorizedMerchantException::class)
    fun handleUnauthorizedMerchant(e: UnauthorizedMerchantException): ResponseEntity<CreateShipmentResponse> {
        logger.warn("Unauthorized merchant access: ${e.message}")
        return ResponseEntity.status(FORBIDDEN)
            .body(CreateShipmentResponse(
                success = false,
                errorMessage = e.message
            ))
    }

    @ExceptionHandler(InsufficientOrderAmountException::class)
    fun handleInsufficientOrderAmount(e: InsufficientOrderAmountException): ResponseEntity<CreateShipmentResponse> {
        logger.warn("Insufficient order amount: ${e.message}")
        return ResponseEntity.status(BAD_REQUEST)
            .body(CreateShipmentResponse(
                success = false,
                errorMessage = e.message
            ))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(e: MethodArgumentNotValidException): ResponseEntity<CreateShipmentResponse> {
        val errors = e.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }

        logger.warn("Validation errors: $errors")
        return ResponseEntity.status(BAD_REQUEST)
            .body(CreateShipmentResponse(
                success = false,
                errorMessage = "Validation failed: $errors"
            ))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<CreateShipmentResponse> {
        logger.warn("Invalid argument: ${e.message}")
        return ResponseEntity.status(BAD_REQUEST)
            .body(CreateShipmentResponse(
                success = false,
                errorMessage = e.message ?: "Invalid request"
            ))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<CreateShipmentResponse> {
        logger.error("Unexpected error occurred", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(CreateShipmentResponse(
                success = false,
                errorMessage = "Internal server error"
            ))
    }
}