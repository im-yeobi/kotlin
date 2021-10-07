package com.example.kotlinstudy.week08

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    isClosedForReceive("isClosedForSend")
    isEmpty("isClosedForSend")
//    closedChannelException("closedChannelException")
//    sendFull("sendFull")
}


private fun isClosedForReceive(method: String) {
    val channel = Channel<Int>()
    log("$method : ${channel.isClosedForSend}") // false
    channel.close()
    log("$method : ${channel.isClosedForSend}") // true
}

private suspend fun isEmpty(method: String) {
    val channel = Channel<Int>(1)
    log("$method : ${channel.isEmpty}") // true
    channel.send(1)
    log("$method : ${channel.isEmpty}") // false
}

private suspend fun closedChannelException(method: String) {
    val channel = Channel<Int>(1)
    channel.close()
    channel.trySendBlocking(10)
}

private suspend fun sendFull(method: String) {
    val channel = Channel<Int>(1)
    log("$method : ${channel.trySendBlocking(2)}")
//    log("$method : ${channel.receive()}")
}
