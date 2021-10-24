package com.example.kotlinstudy.week11

import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking

fun main() = runBlocking<Unit> {

    println(Job)
    println("My job is ${coroutineContext.job.isActive}")
    println("My job is ${coroutineContext[Job]?.isCompleted}")
    println("My job is ${coroutineContext[Job]?.isCancelled}")
    println("My job is ${coroutineContext[Job]?.isActive}")
}
