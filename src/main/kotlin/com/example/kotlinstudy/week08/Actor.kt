package com.example.kotlinstudy.week08

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

private var counterByActor = 0
private val contextActor = newSingleThreadContext("counterActor")

fun main() = runBlocking {
    val workerA = incrementAsyncByActor(2000, "workerA")
    val workerB = incrementAsyncByActor(2000, "workerB")
    workerA.await()
    workerB.await()

    log("counter [$counterByActor]")
}

enum class Action {
    INCREASE,
    DECREASE
}

val actorCounter = GlobalScope.actor<Action>(contextActor) {
    for (msg in channel) {
        when (msg) {
            Action.INCREASE -> counterByActor++
            Action.DECREASE -> counterByActor--
        }
    }
}

fun incrementAsyncByActor(number: Int, worker: String) =
    GlobalScope.async(Dispatchers.IO) {
        for ( i in 0 until number) {
            actorCounter.send(Action.INCREASE)
            log("$worker send $i")
        }
    }
