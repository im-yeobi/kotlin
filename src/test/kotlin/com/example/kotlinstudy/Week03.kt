package com.example.kotlinstudy

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

var count: Int = 0

fun loadEmails(person: Week03.Person): List<String> {
    // FIXME
//    println("email load ${count++}")
    return listOf("123@toss.im")
}

class Week03 {

    @Test
    @DisplayName("compareValuesBy를 사용해서 성적이높고 이름이 빠른순으로 정렬될 수 있게 작성하기")
    fun sort() {
        val actual = listOf<User>(
            User(50, "B"),
            User(50, "C"),
            User(50, "A"),
            User(100, "A"),
            User(100, "B"),
            User(100, "C"),
            User(99, "C"),
            User(99, "B"),
            User(99, "A")
        ).sorted()

        actual[0].grade shouldBe 100
        actual[3].grade shouldBe 99
        actual[6].grade shouldBe 50

        actual[0].name shouldBe "A"
        actual[1].name shouldBe "B"
        actual[2].name shouldBe "C"

        actual[3].name shouldBe "A"
        actual[4].name shouldBe "B"
        actual[5].name shouldBe "C"

        actual[6].name shouldBe "A"
        actual[7].name shouldBe "B"
        actual[8].name shouldBe "C"
    }

    data class User(
        val grade: Int,
        val name: String
    ) : Comparable<User> {
        override fun compareTo(other: User): Int {
            // FIXME
            return compareValuesBy(this, other, User::grade, User::name)
        }
    }

    @Test
    fun lazy() {
        val person = Person("test")
        count shouldBe 0
        person.emails
        count shouldBe 1
        person.emails
        count shouldBe 1
    }

    // FIXME
    data class Person(
        val name: String,
        val emails: List<String>? = null
    ) {
//        val emails: List<String> by lazy { loadEmails(this) }
    }


    @Test
    @DisplayName("delegate 패턴을 이용해서 이메일을 검증해보자")
    fun delegate() {
        val email = Email("email")
        assertDoesNotThrow { email.value = "woojin.kang@toss.im" }
        assertThrows(RuntimeException::class.java) {
            email.value = "woojin.kangtoss.im"
        }
    }

    class Email(
        value: String
    ) {
        var value: String by EmailDelegate(value)
    }

    class EmailDelegate(var curr: String) : ReadWriteProperty<Any?, String> {
        // FIXME
        override fun getValue(thisRef: Any?, property: KProperty<*>): String = curr
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            // email format 검증하기
            if (!value.contains("@")) {
                throw RuntimeException()
            }
            curr = value
        }

    }

    @TestFactory
    fun `고차함수를 이용해서 중복되는 코드를 제거해보자`(): List<DynamicTest> {
        val users = arrayListOf<TossUser>()
        for (i in 1..100) {
            users.add(TossUser(i, i % 2, i % 3 + 1))
        }
        return listOf(DynamicTest.dynamicTest("1반, 2반, 3반에 대해서 평균을 구해보자") {
            // FIXME
            users.averageGradeFor { it.no % 3 == 0 } shouldBe 50
            users.averageGradeFor { it.no % 3 == 1 } shouldBe 51
            users.averageGradeFor { it.no % 3 == 2 } shouldBe 50.5
        }, DynamicTest.dynamicTest("성별로 평균을 구하기") {
            users.averageGradeFor { it.gender % 2 == 0 } shouldBe 51
            users.averageGradeFor { it.gender % 2 == 1 } shouldBe 50
        })
    }

    // FIXME
    fun List<TossUser>.averageGradeFor(predicate: (TossUser) -> Boolean) =
        filter(predicate).map(TossUser::grade).average()

    data class TossUser(
        val grade: Int,
        val gender: Int, // 0: 남자, 1: 여자
        val no: Int
    ) {

    }
}


