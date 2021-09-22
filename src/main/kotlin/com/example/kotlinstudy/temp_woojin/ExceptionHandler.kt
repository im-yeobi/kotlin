package com.example.kotlinstudy.temp_woojin

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking


@OptIn(DelicateCoroutinesApi::class)
fun main() {
    runBlocking() {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("CoroutineExceptionHandler got $exception")
        }
        val job = async(handler) { // root coroutine, running in GlobalScope
            throw RuntimeException()
        }
        val deferred = async(handler) { // also root, but async instead of launch
            throw ArithmeticException() // Nothing will be printed, relying on user to call deferred.await()
        }
        try {
            val awaitAll = awaitAll(job, deferred)
        } catch (e: Exception) {
            log("error $e" )
        }
    }
}
