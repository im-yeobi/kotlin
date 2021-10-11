package com.example.kotlinstudy.week08

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger

fun main() {
    var counter = AtomicInteger()

    runBlocking {
        fun asyncIncrement(by: Int) = async {
            for (i in 0 until by) {
                counter.incrementAndGet()
            }
        }

        val workerA = asyncIncrement(2000)
        val workerB = asyncIncrement(2000)
        workerA.await()
        workerB.await()

        log("counter: $counter")
    }

}
