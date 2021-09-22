package com.example.kotlinstudy.temp_woojin

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

fun main() = runBlocking {
    val async = async(Dispatchers.IO) {
        log("hello")
        delay(1000)
        "async"
    }
    val name = withContext(Dispatchers.IO) {
        delay(1000)
        "woojin"
    }
    log("name : $name, ${async.await()}")
}
