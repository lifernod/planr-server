package org.niikage.planr

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PlanrApplication

fun main(args: Array<String>) {
    runApplication<PlanrApplication>(*args)
}
