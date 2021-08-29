package com.example.kotlinstudy

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Description

class Week04 {


    @Test
    fun `고양이만 필터링하기`() {
        val animalList = listOf<Animal>(Cat("cat1", 1), Cat("cat2", 2), Dog(Color.BLUE, true, "dog1", 3),
                Dog(Color.BLUE, false, "dog2", 4), Dog(Color.GREEN, true, "dog3", 5))

        val result = animalList.filterIsType<Cat>()

        result.size shouldBeExactly 2
        result shouldContain Cat("cat1", 1)
        result shouldContain Cat("cat2", 2)
    }

    // TODO : implement this
    inline fun <reified T> List<Animal>.filterIsType(): List<T> {
        val result = mutableListOf<T>()
        for (element in this) {
            if (element is T) {
                result.add(element)
            }
        }
        return result
    }


    @Test
    @Description("Cat 타입을 json serialize시 name field를 cuteCatName으로, age를 cuteCatAge로 변환")
    fun `annotation 활용하기`() {

        val cat = Cat("cat1", 11)
        val mapper = jacksonObjectMapper()
        val result = mapper.writeValueAsString(cat)
        result shouldBe """{"cuteCatName":"cat1","cuteCatAge":11}"""
    }


    data class Cat(
            @JsonProperty("cuteCatName")
            override val name: String,
            @JsonProperty("cuteCatAge")
            override val age: Int,
    ) : Animal(name, age)

    data class Dog(
            val eyeColor: Color,
            val likeOutsideWalk: Boolean,
            override val name: String, override val age: Int,
    ) : Animal(name, age)

    open class Animal(open val name: String, open val age: Int)

    enum class Color {
        RED, YELLOW, BLUE, GREEN
    }
}

