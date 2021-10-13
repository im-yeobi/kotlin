package com.example.kotlinstudy.week09

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

fun main() {
    runBlocking {
        val test = ContinuationTest()
        test.getUserSummary(1)
    }
}

class ContinuationTest {

    data class UserSummary(
            val profile: Profile,
            val age: Long,
            val terms: String
    )

    data class Profile(
            val dateOfBirth: String,
            val country: String
    )

    suspend fun getUserSummary(id: Int): UserSummary {
        println("fetching summary of $id")
        val profile = fetchProfile(id) // suspending fun
        val age = calculateAge(profile.dateOfBirth)
        val terms = validateTerms(profile.country, age) // suspending fun
        return UserSummary(profile, age, terms)
    }

    suspend fun fetchProfile(id: Int): Profile = withContext(Dispatchers.Default) {
        Profile("2021-01-01", "KR")
    }

    fun calculateAge(dateOfBirth: String): Long {
        return 10
    }

    suspend fun validateTerms(country: String, age: Long): String = withContext(Dispatchers.Default) {
        "terms"
    }

}