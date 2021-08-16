package com.example.kotlinstudy.week03

import java.time.LocalDate

operator fun ClosedRange<LocalDate>.iterator() : Iterator<LocalDate> = object : Iterator<LocalDate> {
    var current = start
    override fun hasNext() = current <= endInclusive


    override fun next() = current.apply {
        current = plusDays(1)
    }
}

//@JvmName("iteratorLong")
//operator fun ClosedRange<Long>.iterator() : Iterator<Long> = object : Iterator<Long> {
//    var current = start
//    override fun hasNext() = current <= endInclusive
//
//
//    override fun next() = current.apply {
//        current++
//    }
//}

data class Tee(val a: Int,
               val b: Int,
               val c: Int,
               val d: Int,
               val e: Int,
               val f: Int,
               val g: Int,
               val h: Int,
               val i: Int,
               val j: Int,
               val k: Int) {

}
fun main() {
    val newYear = LocalDate.ofYearDay(2017, 1)
    val daysOff = newYear.minusDays(1)..newYear
    for (dayOff in daysOff) {
        println(dayOff)
    }

    val first = 10;
    val last = first..100
    for (i in last) {
        println(i)
    }

    val (a,b,c,d,e,f,g,h,i,j,k) = Tee(1,2,3,4,5,6,7,8,9,10,11)
    println(a..k)
}