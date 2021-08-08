package com.example.kotlinstudy

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class Week02 {

    @Test
    @DisplayName("성적이 B 이상이고, 나이는 12살 이상 20살 이하인 학생들을 반별로 묶어서 반환하되 반별로는 이름순으로 정렬한다")
    fun test01() {
        val studentList = ('A'..'Z').map {
            val grade = when (it.code % 4) {
                0 -> 'A'
                1 -> 'B'
                2 -> 'C'
                else -> 'D'
            }
            Student(it, it.code.minus(55), it.code % 5, grade)
        }.toList()

        // FIXME
        val result = studentList.groupBy { it.age }

        assert(result.keys.count() == 3)
        assert(result[0] == null)
        assert(result[1] == null)
        assert(result[2]!!.isNotEmpty() && result[2]!!.count() == 1)
        assert(result[2]!![0] == Student('H', 17, 2, 'A'))
        assert(result[3]!!.isNotEmpty() && result[3]!!.count() == 2)
        assert(result[3]!![0] == Student('D', 13, 3, 'A'))
        assert(result[3]!![1] == Student('I', 18, 3, 'B'))
        assert(result[4]!!.isNotEmpty() && result[4]!!.count() == 1)
        assert(result[4]!![0] == Student('E', 14, 4, 'B'))

    }

}

data class Student(
        val name: Char,
        val age: Int,
        val classNo: Int,
        val grade: Char
) {
    override fun equals(other: Any?): Boolean {
        return other != null &&
                other is Student &&
                this.classNo == other.classNo &&
                this.age == other.age &&
                this.classNo == other.classNo &&
                this.grade == other.grade
    }
}



