package com.example.kotlinstudy.week09

import kotlinx.coroutines.*

//suspend fun main() {
//    val pool = newFixedThreadPoolContext(3, "myPool")
//    val ctx = newSingleThreadContext("ctx")
//
//    val tasks = mutableListOf<Deferred<Unit>>()
//
//    for (i in 0..5) {
//        val task = GlobalScope.async(pool + CoroutineName("main")) {
//            println("Processing $i in ${threadName()}")
//
//            withContext(ctx + CoroutineName("inner")) {
//                println("Step two of $i happening in thread ${threadName()}")
//            }
//
//            println("Finishing $i in ${threadName()}")
//        }
//        tasks.add(task)
//    }
//
//    for (task in tasks) {
//        task.await()
//    }
//}

private fun threadName() = Thread.currentThread().name