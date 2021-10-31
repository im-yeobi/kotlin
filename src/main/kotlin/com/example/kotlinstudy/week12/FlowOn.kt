package com.example.kotlinstudy.week12

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

fun simple5(): Flow<Int> = flow {
    for (i in 1..3) {
        Thread.sleep(1000) // pretend we are computing it in CPU-consuming way
        log("Emitting $i")
        emit(i) // emit next value
    }
}.flowOn(Dispatchers.Default) // RIGHT way to change context for CPU-consuming code in flow builder

fun main() = runBlocking<Unit> {
    simple5().collect { value ->
        log("Collected $value")
    }
}