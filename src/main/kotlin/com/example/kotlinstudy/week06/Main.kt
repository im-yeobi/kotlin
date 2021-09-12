package com.example.kotlinstudy.week06

import kotlinx.coroutines.*
import java.lang.UnsupportedOperationException


fun main() {
    // runBlocking<Unit> { println("My context is: $coroutineContext") }
    // val netDispatcher = newSingleThreadContext(name="ServiceCall")
    runBlocking {
        val task = GlobalScope.async {
            doSomething()
        }
        task.join()
        println("Completed")
    }
    Job()
}

fun doSomething() {
    throw UnsupportedOperationException("Can't do")
}