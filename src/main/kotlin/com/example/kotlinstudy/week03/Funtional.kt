package com.example.kotlinstudy.week03

import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException


@Throws(IOException::class)
fun readFirstLineFromFile(path: String?): String? {
    BufferedReader(FileReader(path)).use { br -> return br.readLine() }
}
fun printNumber(): (Int) -> Unit {
    return { num ->
        if (num % 2 == 0) println("짝수") else {
            println("홀수")
        }
    }
}

data class SiteVisit(
    val path: String,
    val duration: Double,
    val os: String
)

fun List<SiteVisit>.averageDurationFor(os: String) = filter { it.os == os }.map { it.duration }.average()
fun List<SiteVisit>.averageDurationFor(osSet: Set<String>) = filter { it.os in osSet }.map { it.duration }.average()
fun List<SiteVisit>.averageDurationFor(predicate: (SiteVisit) -> Boolean) =
    filter(predicate).map { it.duration }.average()

fun main() {
    val message = printNumber()
    message(5)

}

