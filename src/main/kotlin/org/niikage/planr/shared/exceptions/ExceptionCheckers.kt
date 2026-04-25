package org.niikage.planr.shared.exceptions

import org.springframework.dao.DataIntegrityViolationException

suspend inline fun <R> maybeConflict(
    message: String,
    crossinline block: suspend () -> R
): R {
    return try {
        block()
    } catch (_: DataIntegrityViolationException) {
        throw ConflictException(message)
    }
}

suspend inline fun <R> maybeNotFound(
    message: String,
    crossinline block: suspend () -> R?
): R {
    return block() ?: throw NotFoundException(message)
}