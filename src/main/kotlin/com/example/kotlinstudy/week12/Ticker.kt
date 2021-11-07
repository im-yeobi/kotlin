package com.example.kotlinstudy.week12
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
class Ticker {
    fun main() = runBlocking<Unit> {
        val tickerChannel = ticker(delayMillis = 10000, initialDelayMillis = 0) // create ticker channel
        var nextElement = withTimeoutOrNull(100) { tickerChannel.receive() }
        println("Initial element is available immediately: $nextElement") // no initial delay

        nextElement = withTimeoutOrNull(5000) { tickerChannel.receive() } // all subsequent elements have 100ms delay
        println("Next element is not ready in 50 ms: $nextElement")

        nextElement = withTimeoutOrNull(6000) { tickerChannel.receive() }
        println("Next element is ready in 100 ms: $nextElement")

        // Emulate large consumption delays
        println("Consumer pauses for 150ms")
        delay(15000)
        // Next element is available immediately
        nextElement = withTimeoutOrNull(100) { tickerChannel.receive() }
        println("Next element is available immediately after large consumer delay: $nextElement")
        // Note that the pause between `receive` calls is taken into account and next element arrives faster
        nextElement = withTimeoutOrNull(6000) { tickerChannel.receive() }
        println("Next element is ready in 50ms after consumer pause in 150ms: $nextElement")

        tickerChannel.cancel() // indicate that no more elements are needed

    }
}