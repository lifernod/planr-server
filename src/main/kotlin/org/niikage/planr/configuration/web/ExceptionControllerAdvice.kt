package org.niikage.planr.configuration.web

import org.niikage.planr.shared.exceptions.ApiException
import org.niikage.planr.shared.exceptions.ApiExceptionResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionControllerAdvice {
    @ExceptionHandler(ApiException::class)
    suspend fun handleApiException(e: ApiException): ResponseEntity<ApiExceptionResponse> {
        return ResponseEntity
            .status(e.status)
            .body(ApiExceptionResponse.fromException(e))
    }
}