package com.example.kotlinstudy.week08

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun main() {
    var mutex = Mutex()

    runBlocking {
        fun asyncIncrement(by: Int) = async(Dispatchers.IO) {
            log("thread")
            for (i in 0 until by) {
                log("before")
                mutex.withLock {
                    log("counter")
                    counter++
                }
                log("after")
            }
        }

        val workerA = asyncIncrement(20)
        val workerB = asyncIncrement(20)
        workerA.await()
        workerB.await()

        log("counter $counter")
    }
}
