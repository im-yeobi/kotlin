package com.example.kotlinstudy.week02.sequence


fun main() {

    // sequence
    val sequence = listOf(1, 2, 3, 4).asSequence().map { println("map-sequence($it)"); it * it }
            .filter { println("filter-sequence($it)"); it % 2 == 0 }

    // non sequence
    listOf(1, 2, 3, 4).map { println("map-non sequence($it"); it * it }
            .filter { println("filter-non sequence($it"); it % 2 == 0 }

    // 최종연산이 불릴때 실행
    sequence.toList()
}
