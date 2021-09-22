package com.example.kotlinstudy.temp_woojin

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

fun main() {

    runBlocking(newSingleThreadContext("hello")) {
        val async = async(Dispatchers.IO) {
            log("start1")
            Thread.sleep(1000)
            log("end1")
        }

        val async1 = async {
            log("start2")
            yield()
            Thread.sleep(2000)
            log("end2")
        }

        val async2 = async {
            log("start3")
            yield()
            Thread.sleep(500)
            log("end3")
        }


        async.await()
        async1.await()
        async2.await()
    }
}
