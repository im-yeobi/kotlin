package com.example.kotlinstudy.coroutines

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

suspend fun yieldThreadTimes() {
    log("1")
    delay(1000L)
    yield()
    log("2")
    delay(1000L)
    yield()
    log("3")
    delay(1000L)
    yield()
    log("4")
}

fun suspendExample() {
    log("suspend started")
    GlobalScope.launch { example(5)}
    log("suspend executed")
}

suspend fun example(v: Int): Int {
    return v*2
}
fun main() {
    log("main started")
    suspendExample()
    Thread.sleep(5000L)
    log("main ended")
}
