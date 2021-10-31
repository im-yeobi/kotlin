package com.example.kotlinstudy.week12
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun simple2(): Flow<Int> = flow {
    println("Flow started")
    for (i in 1..3) {
        delay(3000)
        emit(i)
    }
}

fun main() = runBlocking<Unit> {
    println("Calling simple function...")
    val flow = simple2()
    println("Calling collect...")
    flow.collect { value -> println(value) }
    println("Calling collect again...")
    flow.collect { value -> println(value) }
}