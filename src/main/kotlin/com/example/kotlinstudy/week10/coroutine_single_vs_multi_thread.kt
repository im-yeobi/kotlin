package com.example.kotlinstudy.week10

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun main() {
    val singleThreadTime = measureTimeMillis { singleThread() }
    println("single thread time : $singleThreadTime ms")

    val multiThreadTime = measureTimeMillis { multiThread() }
    println("multi thread time : $multiThreadTime ms")
}

fun singleThread() {
    val ctx = newSingleThreadContext("single")
    runBlocking {
        repeat(1_000_000) {
            launch(ctx) {
                delay(100)
            }
        }
    }
}

fun multiThread() {
    val ctx = newFixedThreadPoolContext(10, "multi")
    runBlocking {
        repeat(1_000_000) {
            launch(ctx) {
                delay(100)
            }
        }
    }
}
