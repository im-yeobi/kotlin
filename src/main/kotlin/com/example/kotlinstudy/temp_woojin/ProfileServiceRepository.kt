package com.example.kotlinstudy.temp_woojin

import com.example.kotlinstudy.coroutines.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

data class Profile(
    val id: Long,
    val name: String
)

interface ProfileServiceRepository {
    suspend fun fetchByName(name: String): Profile
    suspend fun fetchById(id: Long): Profile
}

class ProfileServiceClient : ProfileServiceRepository {
    override suspend fun fetchByName(name: String): Profile {
        log("fetchByName")
        val profile = Profile(1, name)
        delay(1000)
        log("fetchByName end")
        return profile
    }

    override suspend fun fetchById(id: Long): Profile {
        log("fetchById")
        val profile = Profile(id, "woojin")
        delay(5000)
        log("fetchByIdend")
        return profile
    }
}

fun main() {
    val profileServiceClient = ProfileServiceClient()
    runBlocking(Dispatchers.Unconfined) {
        val profile = profileServiceClient.fetchById(1)
        val profile2 = profileServiceClient.fetchByName("kotlin")

        println(profile)
        println(profile2)
    }
}
