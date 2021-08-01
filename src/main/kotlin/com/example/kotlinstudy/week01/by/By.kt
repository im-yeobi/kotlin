package com.example.kotlinstudy.week01.by

interface Decorator {
    fun fun1(): Unit
    fun fun2(): Unit
}
class By2(private val by1: By1): Decorator by by1 {

}
class By1: Decorator {
    override fun fun1(){
        println("By1 fun1")
    }

    override fun fun2(){
        println("By1 fun2")
    }
}

fun main() {
    val by2 = By2(By1())
    by2.fun1()
    by2.fun2()

}