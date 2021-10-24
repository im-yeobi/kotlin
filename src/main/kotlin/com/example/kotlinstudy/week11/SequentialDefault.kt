package com.example.kotlinstudy.week11

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

suspend fun doSomethingUsefulOne(): Int {
    delay(1000L) // pretend we are doing something useful here
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L) // pretend we are doing something useful here, too
    return 29
}

fun main() = runBlocking {
    val time = measureTimeMillis {
        log("one before")
        val one = async() {
            doSomethingUsefulOne()
        }
        log("one complete")
        val two = async {
            doSomethingUsefulTwo()
        }
        log("two complete")
        log("The answer is ${one.await() + two.await()}")
    }
    log("Completed in $time xms")
}
