package com.example.kotlinstudy.coroutines

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

lateinit var jobA: Job
lateinit var jobB: Job
fun main() = runBlocking {

    jobA = GlobalScope.launch {
        delay(1000)
        jobB.join()
    }
    jobB = GlobalScope.launch {
        jobA.join()
    }
    jobA.join()
    println("Finished")
}
