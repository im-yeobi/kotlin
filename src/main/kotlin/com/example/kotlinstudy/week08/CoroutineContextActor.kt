package com.example.kotlinstudy.week08

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val bufferedPrinter = actor<String>(newFixedThreadPoolContext(3, "pool")) {
            for (msg in channel) {
                log(msg)
            }
        }

        bufferedPrinter.send("hello")
        bufferedPrinter.send("world")
        bufferedPrinter.close()
    }
}
