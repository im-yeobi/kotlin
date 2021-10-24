package com.example.kotlinstudy.week11

import com.example.kotlinstudy.coroutines.log
import com.example.kotlinstudy.temp_woojin.sleep
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.system.measureTimeMillis

// The result type of somethingUsefulOneAsync is Deferred<Int>
@OptIn(DelicateCoroutinesApi::class)
fun somethingUsefulOneAsync() = GlobalScope.async {
    log("somethingUsefulOneAsync")
    doSomethingUsefulOne()
    log("somethingUsefulOneAsync end")
}

// The result type of somethingUsefulTwoAsync is Deferred<Int>
@OptIn(DelicateCoroutinesApi::class)
fun somethingUsefulTwoAsync() = GlobalScope.async {
    log("somethingUsefulTwoAsync")
    doSomethingUsefulTwo()
    log("somethingUsefulTwoAsync end")
}

// note that we don't have `runBlocking` to the right of `main` in this example
fun main() {
    log("main")
    val time = measureTimeMillis {
        log("measureTimeMillis")
        // we can initiate async actions outside of a coroutine
        val one = somethingUsefulOneAsync()
        val two = somethingUsefulTwoAsync()
        // but waiting for a result must involve either suspending or blocking.
        // here we use `runBlocking { ... }` to block the main thread while waiting for the result
//        runBlocking {
//            log("The answer is ${one.await() + two.await()}")
//        }
    }
    log("Completed in $time ms")
    sleep(1100)
}
