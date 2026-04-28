package org.niikage.planr.shared.utils

import io.r2dbc.spi.Row

inline fun <reified T> Row.required(field: String): T {
    return this.get(field, T::class.java)
        ?: throw IllegalArgumentException("Поля $field не существует")
}

inline fun <reified T> Row.optional(field: String): T? {
    return this.get(field, T::class.java)
}