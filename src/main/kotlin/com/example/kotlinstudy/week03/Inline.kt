package com.example.kotlinstudy.week03

inline fun inlineTest(body: (String) -> Unit) {
    body("Hello inline")
}

inline fun inlineTest2(noinline body: (String) -> Unit) {
    println("outer before")
    val outer = body
    println("outer after")

    outer.apply {
        println("outer")
    }
}
fun main() {
    println("before")
    inlineTest { str -> println(str) }
    println("after")

    println("before")
    inlineTest2 { str -> println(str) }
    println("after")
}