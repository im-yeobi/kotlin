package com.example.kotlinstudy.week08

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    isClosedForReceive("isClosedForSend")
    closedReceiveChannelException("closedReceiveChannelException")
}


private fun isClosedForReceive(method: String) {
    val channel = Channel<Int>()
    log("$method : ${channel.isClosedForReceive}") // false
    channel.close()
    log("$method : ${channel.isClosedForReceive}") // true
}

private suspend fun closedReceiveChannelException(method: String) {
    val channel = Channel<Int>(1)
    channel.close()
    channel.receive()
}

private suspend fun sendFull(method: String) {
    val channel = Channel<Int>(1)
    log("$method : ${channel.trySendBlocking(2)}")
//    log("$method : ${channel.receive()}")
}
