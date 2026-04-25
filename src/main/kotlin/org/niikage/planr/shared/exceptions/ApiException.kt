package org.niikage.planr.shared.exceptions

import org.springframework.http.HttpStatus

sealed class ApiException(
    override val message: String,
    val status: HttpStatus,
) : RuntimeException(message)

class NotFoundException(
    message: String
) : ApiException(message, HttpStatus.NOT_FOUND)

class ConflictException(
    message: String
) : ApiException(message, HttpStatus.CONFLICT)

class BadRequestException(
    message: String
) : ApiException(message, HttpStatus.BAD_REQUEST)