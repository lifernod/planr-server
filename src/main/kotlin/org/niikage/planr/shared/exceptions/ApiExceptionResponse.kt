package org.niikage.planr.shared.exceptions

import java.time.OffsetDateTime

data class ApiExceptionResponse(
    val exception: String,
    val message: String,
    val statusCode: Int,
    val statusText: String,
    val timestamp: OffsetDateTime = OffsetDateTime.now()
) {
    companion object {
        fun fromException(e: ApiException): ApiExceptionResponse {
            return ApiExceptionResponse(
                exception = e.javaClass.simpleName,
                message = e.message,
                statusCode = e.status.value(),
                statusText = e.status.reasonPhrase
            )
        }
    }
}
