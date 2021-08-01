package com.example.kotlinstudy.week01.inter

interface Focusable {
    fun showOff() = println("showOff")
}

class Button: Focusable {
    override fun showOff() {
        super.showOff()
    }
}

fun main() {
    Button().showOff()
}