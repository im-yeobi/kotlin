package com.example.kotlinstudy.temp_woojin

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

fun main() = runBlocking {
    val duration = measureTimeMillis {
        val job = launch {
            try {
                while(isActive){
                    delay(500)
                    log("still running")
                }
            } finally {
                withContext(NonCancellable) {
                    log("cancelled, will end now")
                    delay(5000)
                    log("delay completed, bye bye")
                }

            }
        }
        delay(1200)
        job.cancelAndJoin()
    }
    log("Took $duration ms")
}
