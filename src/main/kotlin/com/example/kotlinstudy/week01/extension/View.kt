package com.example.kotlinstudy.week01.extension

open class View {
}

class Button: View() {

}

fun View.showOff() = println("I'm a View!")
fun Button.showOff() = println("I'm a Button!")

fun main() {
    val view: View = Button()
    view.showOff()
}