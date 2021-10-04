package com.example.kotlinstudy.week08

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        createActor(CoroutineStart.LAZY)
        createActor(CoroutineStart.DEFAULT)
        createActor(CoroutineStart.ATOMIC)
        createActor(CoroutineStart.UNDISPATCHED)
    }
}

private suspend fun CoroutineScope.createActor(start : CoroutineStart) {
    val defaultActor = actor<String>(start = start) {
        for (msg in channel) {
            log(msg)
        }
    }
    log("started ${defaultActor.toString()}")
    log(defaultActor.toString())
    defaultActor.send("hello")
    log(defaultActor.toString())
    defaultActor.send("world")
    defaultActor.close()
    log("closed ${defaultActor.toString()}")
}
