package com.example.kotlinstudy.week08

import kotlin.jvm.Volatile

class Volatile {
    @Volatile
    private var type = 0
    private var title = ""

    fun setTitle(newTitle: String) {
        when(type) {
            0 -> title = newTitle
            else -> throw Exception("invalid state")
        }
    }
}

class DataProcessor {
    @Volatile
    private var shutdownRequested = false

    fun shutdown() {
        shutdownRequested = true
    }

    fun process() {
        while(shutdownRequested) {
            // process away
        }
    }
}
