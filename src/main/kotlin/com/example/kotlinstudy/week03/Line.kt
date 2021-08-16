package com.example.kotlinstudy.week03

import java.time.LocalDate

data class Line(val first: Int, val last: Int) {
    operator fun contains(value: Int): Boolean {
        return value in first..last
    }
}

fun main() {
    println(5 in Line(1,10))
    println(11 in Line(1,10))
    println(0 in Line(1,10))

    val now = LocalDate.now()
    val vacation = now..now.plusDays(10)
    println(now.plusWeeks(1) in vacation)
}