package com.example.kotlinstudy.week01.companion

interface Factory {
    fun factory()
}

class A {
    companion object Name : Factory {
        fun bar() {
            println("bar")
        }

        override fun factory() {
            TODO("Not yet implemented")
        }
    }
}

fun A.Name.printTest() = println("printTest")

fun main() {
    println(A is Factory)
    A.printTest()


}