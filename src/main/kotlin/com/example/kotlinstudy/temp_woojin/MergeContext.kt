package com.example.kotlinstudy.temp_woojin

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

fun main() {
    val dispatcher = newSingleThreadContext("myDispatcher")
    val handler = CoroutineExceptionHandler{ _, throwable ->
        log("Error captured ${throwable.message}")
    }
    runBlocking(dispatcher + handler) {
        GlobalScope.launch(handler) {
            log("Running in ${Thread.currentThread().name}")
            TODO("not implemented")
        }.join()
    }
}
