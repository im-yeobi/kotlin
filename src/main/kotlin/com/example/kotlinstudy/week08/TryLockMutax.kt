package com.example.kotlinstudy.week08

import com.example.kotlinstudy.coroutines.log
import com.example.kotlinstudy.temp_woojin.sleep
import kotlinx.coroutines.async
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


val mutex = Mutex()
val counterFixedThreadPool = newFixedThreadPoolContext(4, "counter")

data class Count(
    var count: Int = 0,
    val name: String
) {
    fun incCount() {
        log("$name 카운트 증가 시작 $count")
        sleep(1000)
        count++
        log("$name 카운트 증가 완료 $count")
    }
}

fun main() {
    // 같은 행동을 하는데 한 요청당 처리는 1초가 걸림
    // 한번에 요청이 여러개가 올 수 있는 상태

    // A 유저
    val countList = arrayListOf<Count>()
    for (i in 0..3) {
        val count = Count(name = "$i")
        countList.add(count)
        createAndStart(count)
    }


    countList.forEach {
        log("count : ${it.count}")
    }
}

private fun createAndStart(count: Count): Count {
    val threads = arrayListOf<Thread>()
    for (i in 0 until 5) {
        threads.add(Thread(startIncCount(count)))
    }
    threads.forEach {
        it.start()
    }
    return count
}

private fun startIncCount(count: Count) =
    Runnable {
        runBlocking {
            val async = async(counterFixedThreadPool) {
                mutex.withLock() {
                    incCount(count)
                }

            }
            async.await()
        }
    }


private fun incCount(count: Count) {
    count.incCount()
}
