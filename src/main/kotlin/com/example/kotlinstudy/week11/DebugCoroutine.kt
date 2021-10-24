package com.example.kotlinstudy.week11

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {
    val a = async {
        log("I'm computing a piece of the answer")
        6
    }
    val b = async {
        log("I'm computing another piece of the answer")
        7
    }
    log("The answer is ${a.await() * b.await()}")
}

