package com.example.kotlinstudy.week08

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis


fun main() = runBlocking(Dispatchers.IO) {
    val time = measureTimeMillis {
        // public const val UNLIMITED: Int = Int.MAX_VALUE
//        val channel = Channel<Int>(Channel.UNLIMITED, onUndeliveredElement = { value ->
//            log("unDelieveredValue $value")
//        })
        val channel = Channel<Int>(Channel.UNLIMITED)
        val sender = launch {
            repeat(5) {
                log("Sent $it")
                channel.send(it)
            }
        }
    }
    delay(500)
    log("Took ${time}ms")
}
