package com.example.kotlinstudy.week01.vararg


fun varargTest(vararg nums: Int): Int {
    return nums.sum()
}

fun main() {
    println(varargTest(1,2,3,4,5))

    val args = arrayOf<String>("1", "2", "3")
    val list = listOf("args: ", *args)
    println(list)
}
