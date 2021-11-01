package com.example.kotlinstudy.week12

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun simple(): Flow<Int> = flow { // flow builder
    for (i in 1..3) {
       // delay(1000) // pretend we are doing something useful here
        Thread.sleep(1000)

        emit(i) // emit next value
    }
}


fun main() = runBlocking<Unit> {
    // Launch a concurrent coroutine to check if the main thread is blocked
    launch {
        for (k in 1..3) {
            println("I'm not blocked $k")
            delay(1000)
        }
    }
    // Collect the flow
    simple().collect { value -> println(value) }
}