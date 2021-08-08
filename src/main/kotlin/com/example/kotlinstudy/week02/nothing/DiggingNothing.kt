package com.example.kotlinstudy.week02.nothing

import java.lang.RuntimeException

class DiggingNothing {

    // return 이 없으면 <A 'return' expression required in a function with a block body ('{...}')>
    // return 을 쓰면 <This function must return a value of type Nothing>
//    fun nothing(): Nothing {
//        println("nothing test")
//        return Nothing  // Classifier 'Nothing' does not have a companion object, and thus must be initialized here
//    }

    // this is ok
    fun nothing2() : Nothing {
        throw RuntimeException()
    }

    // 이것도 문제 없음
    fun nothing3() {
        throw RuntimeException()
    }
}