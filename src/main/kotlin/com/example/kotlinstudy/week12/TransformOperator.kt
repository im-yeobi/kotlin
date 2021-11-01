package com.example.kotlinstudy.week12


import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

suspend fun performRequest2(request: Int): String {
    delay(1000) // imitate long-running asynchronous work
    return "response $request"
}

fun main() = runBlocking<Unit> {
    (1..3).asFlow() // a flow of requests
            .transform { request ->
                emit("Making request $request")
                emit(performRequest2(request))
            }
            .collect { response -> println(response) }
}
