package com.example.sanrio

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SanRioApplication

fun main(args: Array<String>) {
	runApplication<SanRioApplication>(*args)
}
