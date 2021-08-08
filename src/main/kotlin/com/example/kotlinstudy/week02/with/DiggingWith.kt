package com.example.kotlinstudy.week02.with

import java.lang.StringBuilder

fun main() {

    fun alphabet(): String {
        val stringBuilder = StringBuilder()
        return with(stringBuilder)
        {
            for (letter in 'A'..'Z') {
                this.append(letter)
            }
            this.toString()
        }
    }
}
