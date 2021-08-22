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

    @Test
    @DisplayName("나이를 나누기 4한 값이 같은 사람들끼리 한 조기 되었다. 사람들을 데리고 조를 만들어라. 그리고 각 조에 대해서 이 사람들이 핸드폰을 가지고 있는지 검사를 해라. \n" +
            "만약 핸드폰이 없다면 <no-phone>이라고 출력하고 핸드폰이 있다면 번호를 출력하라. ")
    fun test02() {
        val personList = (10..40).map { i ->
            Person(
                    // 나이 % 4 == 0 인 사람은 없다.
                    if (i % 4 == 0) i + 1 else i,
                    if (i % 4 == 2) null else "010-123-456")
        }

        // FIXME : personList를 이용해 나이를 나누기 4한 값이 같은 사람들끼리 한조로 만들어라. personMap의 type 은 변경해도 된다.
        val personMap: Map<Int, List<Person>> = mapOf(1 to listOf(), 2 to listOf())

        assert(personMap.size == 3)
        assert(personMap[0] == null)

        
        personMap.flatMap { it.value }
                .forEach {
                    if (it.age % 4 == 2)
                        assert(it.toPhoneNumber().equals("no-phone"))
                    else
                        assert(it.toPhoneNumber().equals("010-123-456"))
                }
    }

    // FIXME : 만약 핸드폰이 없다면 <no-phone>이라고 출력하고 핸드폰이 있다면 번호를 출력하라. return type은 맞춰서 변환해라
    fun Person.toPhoneNumber(): Any {

    }

}

data class Person(
        val age: Int,
        val phoneNumber: String?
)


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



