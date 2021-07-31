package com.example.kotlinstudy.week01.strings

@JvmOverloads
fun <T> Collection<T>.joinToString(
    separator: String = ", ",
    prefix: String = "",
    postfix: String = ""
): String {
    val result = StringBuilder(prefix)
    for ((index, element) in this.withIndex()) {
        if (index > 0) result.append(separator)
        result.append(element)
    }
    result.append(postfix)
    return result.toString()
}


//fun String.lastChar(): Char = this[this.length -1]
fun String.lastChar(): Char = get(length - 1)