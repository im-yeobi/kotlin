package com.example.kotlinstudy.`week09-test`

import com.example.kotlinstudy.week09.DataSource
import com.example.kotlinstudy.week09.UserManager
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test

class SampleAppFt {

    @Test
    fun testHappyPath() = runBlocking {
        val manager = UserManager(MockDataSource())
        val user = manager.getUser(10)
        assert(user.name == "A")
        assert(user.age == 100)
        assert(user.profession == "B")
    }

    @Test
    fun testOppositeOrder() = runBlocking {
        val manager = UserManager(SlowMockDataSource())
        val user = manager.getUser(10)
        assert(user.name == "A")
        assert(user.age == 100)
        assert(user.profession == "B")
    }
}

class MockDataSource : DataSource {
    override fun getNameAsync(id: Int): Deferred<String> {
        return GlobalScope.async {
            delay(200)
            "A"
        }
    }

    override fun getAgeAsync(id: Int): Deferred<Int> {
        return GlobalScope.async {
            delay(500)
            100
        }
    }

    override fun getProfessionAsync(id: Int): Deferred<String> {
        return GlobalScope.async {
            delay(2000)
            "B"
        }
    }
}

class SlowMockDataSource : DataSource {
    override fun getNameAsync(id: Int): Deferred<String> {
        return GlobalScope.async {
            delay(1000)
            "A"
        }
    }

    override fun getAgeAsync(id: Int): Deferred<Int> {
        return GlobalScope.async {
            delay(500)
            100
        }
    }

    override fun getProfessionAsync(id: Int): Deferred<String> {
        return GlobalScope.async {
            delay(200)
            "B"
        }
    }
}