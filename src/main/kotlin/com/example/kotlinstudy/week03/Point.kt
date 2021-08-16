package com.example.kotlinstudy.week03

data class Point(var x: Int, var y: Int) {
    operator fun plus(other: Point): Point {
        return Point(x + other.x, y + other.y)
    }

    operator fun plusAssign(other: Point): Unit {
        x += other.x
        y += other.y
    }

    operator fun minus(other: Point): Point {
        return Point(x - other.x, y - other.y)
    }

    operator fun times(other: Point): Point {
        return Point(x - other.x, y - other.y)
    }

    operator fun div(other: Point): Point {
        return Point(x - other.x, y - other.y)
    }

    operator fun unaryPlus(): Point {
        return Point(+x ,+y)
    }

    operator fun unaryMinus(): Point {
        return Point(-x , -y)
    }

    operator fun not(): Point {
        return Point(-x , -y)
    }

    operator fun inc(): Point {
        return Point(x+1 , y+1)
    }

    operator fun dec(): Point {
        return Point(x-1 , y-1)
    }
}

fun main() {
    val p1 = Point(10,20)
    val p2 = Point(30, 40)
    println(p1+p2)
    p1+=p2
    println(p1)

    val list = arrayListOf(1,2)
    list += 3
    val newList = list + listOf(4,5)
    println(list)
    println(newList)
}