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
        val channel = Channel<Int>(4)
        val sender = launch {
            repeat(10) {
                log("Sent $it")
                channel.send(it)
            }
        }
        delay(500)
        log("Taking two")
        repeat(2) {
            val data = channel.receive()
            log("received $data")
        }
        delay(500)
    }
    log("Took ${time}ms")
}
