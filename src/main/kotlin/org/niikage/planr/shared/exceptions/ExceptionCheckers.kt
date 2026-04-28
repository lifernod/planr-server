package org.niikage.planr.shared.exceptions

import io.r2dbc.spi.R2dbcException
import org.springframework.dao.DuplicateKeyException

suspend inline fun <R> maybeViolation(
    message: String,
    crossinline block: suspend () -> R
): R {
    return try {
        block()
    } catch (e: R2dbcException) {
        when (e.sqlState) {
            "23503" -> throw NotFoundException("Связь не найдена: $message")
            else -> throw RuntimeException(e)
        }
    } catch (_: DuplicateKeyException) {
        throw ConflictException("Нарушена уникальность: $message")
    }
}

suspend inline fun <R> maybeNotFound(
    message: String,
    crossinline block: suspend () -> R?
): R {
    return block() ?: throw NotFoundException(message)
}