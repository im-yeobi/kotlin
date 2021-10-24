package com.example.kotlinstudy.week11

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun main() = runBlocking {
    val time = measureTimeMillis {
        val one = async(start = CoroutineStart.LAZY) {
            doSomethingUsefulOne()
        }
        val two = async(start = CoroutineStart.LAZY) {
            doSomethingUsefulTwo()
        }
        one.start()
        two.start()
        log("The answer is ${one.await() + two.await()}")
    }
    log("Completed in $time ms")
}
