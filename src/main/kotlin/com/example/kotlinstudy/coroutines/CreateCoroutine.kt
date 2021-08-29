package com.example.kotlinstudy.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun createCoroutines(amount: Long) {
    runBlocking {
        log("${Thread.activeCount()} threads active at the start")

        val jobs = arrayListOf<Job>()
        for (i in 1..amount) {
//            jobs += launch {
//            jobs += launch(Dispatchers.IO) {
//            jobs += launch(Dispatchers.Unconfined) {
            jobs += launch(Dispatchers.Default) {
                log("launch started $i")
                delay(1000)
                log("launch ended $i")
            }
        }
        jobs.forEach {
            it.join()
        }
        log("${Thread.activeCount()} threads active at the end")
    }

}

fun main() {
    val time = measureTimeMillis {
        createCoroutines(2)
    }
    log("Took $time ms")
}
