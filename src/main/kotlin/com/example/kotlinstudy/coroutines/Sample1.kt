package com.example.kotlinstudy.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

fun now() = ZonedDateTime.now().toLocalTime().truncatedTo(ChronoUnit.MILLIS)

fun log(msg: String) = println("${now()}:${Thread.currentThread()}: ${msg}")

fun launchInGlobalScope() {
    GlobalScope.launch {
        log("coroutine started.")
    }
}

fun runBlockingExample() {
    runBlocking {
        launch {
            log("GlobalScope.launch started.")
        }
    }
}

fun yieldExample() {
    runBlocking {
        async {
            log("1")
            yield()
            log("3")
            yield()
            log("5")
        }
        log("after first launch")
        launch {
            log("2")
            delay(1000L)
            log("4")
            delay(1000L)
            log("6")
        }
        log("after second launch")
        launch {
            log("7")
            yield()
            log("8")
            yield()
            log("9")
        }
        log("after third launch")
    }
}

fun sumAll() {
    runBlocking {
        val d1 = async {
            log("execute d1")
            delay(1000L)
            log("executed d1")
            1
        }
        log("after async(d1)")

        val d3 = async { log("execute d3")
            delay(1000L)
            log("executed d3")
            3 }
        log("after async(d3)")

        val d2 = async { log("execute d2")
            delay(1000L)
            log("executed d2")
            2 }
        log("after async(d2)")

//        log("1+2+3 = ${d1.await() + d2.await() + d3.await()}")
        log("after wait all & add")
    }
}

fun printContext() {
    runBlocking {
        launch {
            log("main runBlocking: I'm working in thread")
        }
        log("after launch")
        launch(Dispatchers.Unconfined) {
            log("Unconfined: I'm working in thread ")
        }
        log("after Unconfined")
        launch(Dispatchers.Default) {
            log("Default: I'm working in thread ")
        }
        log("after Default")
        launch(Dispatchers.IO) {
            log("IO: I'm working in thread ")
        }
        log("after IO")
        launch(newSingleThreadContext("MyOwnThread")) {
            log("newSingleThreadContext: I'm working in thread ")
        }
        log("after newSingleThreadContext")
    }
}
fun main() {
    log("main() started")
//    launchInGlobalScope()
//    runBlockingExample()
//    yieldExample()
//    log("yieldExample() executed")
//    Thread.sleep(5000L)

//    sumAll()
//    log("sumAll() executed")

    printContext()
    log("main() terminated")
}
