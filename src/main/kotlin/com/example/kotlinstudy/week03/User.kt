package com.example.kotlinstudy.week03

data class User(
    val grade: Int,
    val name: String
) : Comparable<User> {
    override fun compareTo(other: User): Int {
//        if (grade.compareTo(other.grade) == 0) {
//            return name.compareTo(other.name)
//        }
//        return other.grade.compareTo(grade)
        return compareValuesBy(this, other, {  other.grade .compareTo(it.grade)}, {  it.name })
    }
}

fun main() {
    listOf<User>(
        User(50, "B"), User(50, "C"), User(50, "A"),
        User(100, "A"), User(100, "B"), User(100, "C"),
        User(99, "C"), User(99, "B"), User(99, "A")
    ).sorted().forEach{ println(it)}
}