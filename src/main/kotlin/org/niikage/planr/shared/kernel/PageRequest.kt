package org.niikage.planr.shared.kernel

data class PageRequest(
    val limit: Int = 100,
    val offset: Int = 0,
)
