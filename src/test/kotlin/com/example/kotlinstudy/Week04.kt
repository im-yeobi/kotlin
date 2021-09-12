package com.example.kotlinstudy

import com.example.kotlinstudy.week01.strings.joinToString
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.io.JsonStringEncoder
import com.fasterxml.jackson.core.json.JsonGeneratorImpl
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Description
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

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
    inline fun <T> List<Animal>.filterIsType(): List<Animal> {
       return this.filterIsInstance<Cat>()
    }

//    inline fun <T> List<Animal>.filterIsType(): List<Animal> {
//        return this.filter {
//            it.javaClass.kotlin.simpleName == Cat::class.simpleName
//        }
//    }

    @Test
    @Description("Cat 타입을 json serialize시 name field를 cuteCatName으로, age를 cuteCatAge로 변환")
    fun `annotation 활용하기`() {
        val cat = Cat("cat1", 11)
//        val mapper = jacksonObjectMapper()
//        val result = mapper.writeValueAsString(cat)

        val objectMapper = ObjectMapper()
        val simpleModule = SimpleModule()
            .addSerializer(Cat::class.java, CatSerializer())
        objectMapper.registerModule(simpleModule)

        val result = objectMapper.writeValueAsString(cat)

        println(result)
        result shouldBe """{"cuteCatName":"cat1","cuteCatAge":11}"""
    }


    data class Cat(
        @property:JsonName(name = "cuteCatName")
//        @field:JsonProperty("cuteCatName")
        override val name: String,
        @property:JsonName(name = "cuteCatAge")
//        @field:JsonProperty("cuteCatAge")
        override val age: Int,
    ) : Animal(name, age)

    annotation class JsonName(val name: String)

    class CatSerializer : JsonSerializer<Cat>() {
        override fun serialize(cat: Cat, generator: JsonGenerator, provider: SerializerProvider) {
            generator.writeStartObject()

            val kClass = cat.javaClass.kotlin
            val properties = kClass.memberProperties

            properties.reversed().forEach { property ->
                val jsonName = property.findAnnotation<JsonName>()
                println(jsonName)
                val propName = jsonName?.name ?: property.name

                generator.writeFieldName(propName)
                generator.writeObject(property.get(cat))
            }

            generator.writeEndObject()
        }

    }

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
