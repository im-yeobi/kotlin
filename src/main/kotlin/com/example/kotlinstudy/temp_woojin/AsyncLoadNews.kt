package com.example.kotlinstudy.temp_woojin

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*

data class Article(
    val title: String
)


fun main() {
    val requests = mutableListOf<Deferred<List<Article>>>()
    runBlocking(Dispatchers.IO) {
        requests.add(async {
            log("create articles")
            val articles = listOf(Article(UUID.randomUUID().toString()))
            log("created articles")
            articles
        })
    }
    val articles = requests.filter { !it.isCancelled }
        .flatMap { it.getCompleted() }

    articles.forEach { println(it) }
}
