package com.example.kotlinstudy

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
@DelicateCoroutinesApi
class Week07_jongyeob {
    @Test
    fun producerTest() {
        runBlocking {
            var transactions: List<Transaction>
            do {
                val producer = getProducer()

                val tossbankTransaction = producer.receive()
                val wooriBankTransaction = producer.receive()
                val kakaoBankTransaction = producer.receive()

                transactions = listOfNotNull(
                    tossbankTransaction.await(),
                    wooriBankTransaction.await(),
                    kakaoBankTransaction.await()
                )

                println()
            } while (transactions.isNotEmpty())
        }
    }

    private fun getProducer() =
        GlobalScope.produce(Dispatchers.IO) {
            accounts.forEach {
                val transaction = async {
                    getTransactionByAccount(it)
                }
                send(transaction)
            }
        }

    private suspend fun getTransactionByAccount(account: Account): Transaction? =
        when (account.bankType) {
            TOSS_BANK -> {
                delay(2000)
                tossBankTransactions.poll()
                    .also { println(it) }
            }
            WOORI_BANK -> {
                delay(500)
                wooriBankTransactions.poll()
                    .also { println(it) }
            }
            KAKAO_BANK -> {
                delay(1000)
                kakaoBankTransactions.poll()
                    .also { println(it) }
            }
            else -> throw RuntimeException()
        }
}
