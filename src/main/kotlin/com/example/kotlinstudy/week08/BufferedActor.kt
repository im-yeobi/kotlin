package com.example.kotlinstudy.week08

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val bufferedPrinter = actor<String>(capacity = 10) {
            for (msg in channel) {
                log(msg)
            }
        }

        log(bufferedPrinter.toString())
        bufferedPrinter.send("hello")
        log(bufferedPrinter.toString())
        bufferedPrinter.send("world")
        bufferedPrinter.close()
    }
}
