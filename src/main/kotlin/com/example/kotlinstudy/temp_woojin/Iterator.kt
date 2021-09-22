package com.example.kotlinstudy.temp_woojin

import com.example.kotlinstudy.coroutines.log

fun main() {
    log("main start")
    val iterator = iterator {
        log("First start")
        yield(getValue("First"))
        log("Second start")
        yield(getValue("Second"))
        log("Third start")
        yield(getValue("Third"))
    }

    log("main ing")
//    iterator.hasNext()
    log(iterator.next())
//
//    Thread.sleep(2000)
    if( iterator.hasNext()) {
        log("hasNext")
        Thread.sleep(2000)
        log(iterator.next())
    }
//    log(iterator.next())
//    log(iterator.next())
    log("main end")
}


fun getValue(str: String): String {
    Thread.sleep(1000)
    return str
}
