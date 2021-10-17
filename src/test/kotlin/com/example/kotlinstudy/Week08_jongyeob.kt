package com.example.kotlinstudy

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

class Week08_jongyeob {
    @ExperimentalCoroutinesApi
    @Test
    fun channelStudy() {
        runBlocking {
            val time = measureTimeMillis {
                val channel = Channel<Int>(capacity = 50, onBufferOverflow = BufferOverflow.DROP_LATEST)
                launch {
                    repeat(100) {
                        if (it == 0)
                            print("[Send 1st] : ")
                        if (it == 50)
                            print("\n[Send 2nd] : ")

                        channel.send(it + 1)
                        print("${it + 1} ")
                    }
                }

                delay(500)
                repeat(100) {
                    if (it == 0)
                        print("\n\n[Receive] : ")

                    if (channel.isEmpty) {
                        channel.close()
                        println("\n[Channel closed]: ${it + 1}")
                        return@measureTimeMillis
                    }
                    print("${channel.receive()} ")
                }
            }

            println("\n[Measure time] : $time ms")
        }
    }
}
