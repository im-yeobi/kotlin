package com.example.kotlinstudy.week09

import kotlinx.coroutines.Deferred

interface DataSource {
    fun getNameAsync(id: Int): Deferred<String>
    fun getAgeAsync(id: Int): Deferred<Int>
    fun getProfessionAsync(id: Int): Deferred<String>
}

data class User(
        val name: String,
        val age: Int,
        val profession: String
)

class UserManager(private val dataSource: DataSource) {
    suspend fun getUser(id: Int): User {
        val name = dataSource.getNameAsync(id)
        val age = dataSource.getAgeAsync(id)
        val profession = dataSource.getProfessionAsync(id)

        profession.await()
        name.await()
        age.await()

        return User(
                name.getCompleted(),
                age.getCompleted(),
                profession.getCompleted()
        )
    }
}