package com.example.kotlinstudy.temp_woojin

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking() {
        log("produce start")
        val producer = produce() {
            send(async(Dispatchers.IO) {
                sleep(5000)
            })
            send(async(Dispatchers.IO) {
                sleep(3000)
            })
            send(async(Dispatchers.IO) {
                sleep(1000)
            })
        }
        log("produce end")

        log("consume start")
        producer.consumeEach {
            log("consume ${it.await()}")
        }
        log("consume end")
    }

}


fun sleep(sleep: Long): String {
    log("sleep start $sleep")
    Thread.sleep(sleep)
    return sleep.toString()
}

