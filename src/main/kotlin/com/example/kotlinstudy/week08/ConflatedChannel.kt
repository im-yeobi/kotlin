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
        // public const val CONFLATED: Int = -1
        val channel = Channel<Int>(Channel.CONFLATED)
        val sender = launch {
            repeat(5) {
                log("Sent $it")
                channel.send(it)
            }
        }
        delay(500)
        repeat(1) {
            val data = channel.receive()
            log("received $data")
        }
    }
    log("Took ${time}ms")
}
