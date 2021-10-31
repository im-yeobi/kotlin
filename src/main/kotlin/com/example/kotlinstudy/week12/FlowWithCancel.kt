package com.example.kotlinstudy.week12

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

fun simple3(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(1000)
        println("Emitting $i")
        emit(i)
    }
}

fun main() = runBlocking<Unit> {
    withTimeoutOrNull(2500) { // Timeout after 250ms
        simple3().collect { value -> println(value) }
    }
    println("Done")
}