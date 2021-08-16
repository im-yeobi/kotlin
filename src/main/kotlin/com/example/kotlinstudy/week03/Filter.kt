package com.example.kotlinstudy.week03

fun String.filter(predicate: (Char) -> Boolean): String {
    val sb = StringBuilder()
    for (index in 0 until length) {
        val element = get(index)
        if (predicate(element)) sb.append(element)
    }
    return sb.toString()
}

fun default(value: Long, operator: (a: Long) -> String = { it.toString()}) {
    println(operator.invoke(value))
}

fun default2(value: Long, operator: ((a: Long) -> String)? = null ) {
    println(operator?.invoke(value) ?: value.toString())
}

fun main() {
    println("ab1c".filter { it in 'a'..'z' })


    default(10) { (it * it).toString() }
}