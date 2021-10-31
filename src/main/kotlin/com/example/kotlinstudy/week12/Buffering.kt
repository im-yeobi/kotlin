package com.example.kotlinstudy.week12

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun simple6(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(1000) // pretend we are asynchronously waiting 100 ms
        emit(i) // emit next value
    }
}




fun main() = runBlocking<Unit> {
    val time = measureTimeMillis {
        simple6()
                .conflate() // conflate emissions, don't process each one
                .collect { value ->
                    delay(3000) // pretend we are processing it for 300 ms
                    println(value)
                }
    }
    println("Collected in $time ms")

//    val time = measureTimeMillis {
//        simple6()
//                .buffer() // buffer emissions, don't wait
//                .collect { value ->
//                    delay(3000) // pretend we are processing it for 300 ms
//                    println(value)
//                }
//    }
//    println("Collected in $time ms")

//    val time = measureTimeMillis {
//        simple6().collect { value ->
//            delay(3000) // pretend we are processing it for 300 ms
//            println(value)
//        }
//    }
//    println("Collected in $time ms")
}