package com.example.kotlinstudy.week08

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking


fun main() = runBlocking {
//    val workerA = incrementAsync(2000)
//    val workerB = incrementAsync(2000)
    val workerA = incrementAsyncBySingleThread(2000)
    val workerB = incrementAsyncBySingleThread(2000)
    workerA.await()
    workerB.await()

    log("counter [$counter]")
}

var counter = 0

fun incrementAsync(number: Int) =
    GlobalScope.async(Dispatchers.IO) {
        for ( i in 0 until number) {
            counter ++
        }
    }

val context = newSingleThreadContext("counter")
fun incrementAsyncBySingleThread(number: Int) =
    GlobalScope.async(context) {
        for ( i in 0 until number) {
            counter ++
        }
    }
