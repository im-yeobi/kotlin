package com.example.kotlinstudy

import java.time.LocalDateTime
import java.util.*

val TOSS_BANK = "토스뱅크"
val WOORI_BANK = "우리은행"
val KAKAO_BANK = "카카오뱅크"

data class Account(
    val bankType: String,
    val accountNumber: String
)

data class Transaction(
    val account: Account,
    val transactionType: String,
    val amount: Int,
    val balance: Int,
    val transactedAt: LocalDateTime
)

val accounts = listOf(
    Account(TOSS_BANK, "1000-0000-1234"),
    Account(WOORI_BANK, "1002-1234-5678"),
    Account(KAKAO_BANK, "3333-01-1234567")
)

val tossBankTransactions: Queue<Transaction> = LinkedList<Transaction>()
    .apply {
        add(Transaction(accounts[0], "입금", 3000, 103000, LocalDateTime.of(2021, 1, 10, 15, 0, 0)))
        add(Transaction(accounts[0], "출금", 13000, 90000, LocalDateTime.of(2021, 1, 10, 17, 0, 0)))
        add(Transaction(accounts[0], "출금", 50000, 40000, LocalDateTime.of(2021, 1, 10, 20, 0, 0)))
    }

val wooriBankTransactions: Queue<Transaction> = LinkedList<Transaction>()
    .apply {
        add(Transaction(accounts[1], "입금", 1, 1, LocalDateTime.of(2021, 1, 1, 7, 0, 0)))
        add(Transaction(accounts[1], "입금", 200000, 200001, LocalDateTime.of(2021, 1, 10, 20, 0, 0)))
        add(Transaction(accounts[1], "출금", 50000, 150001, LocalDateTime.of(2021, 1, 15, 23, 0, 0)))
        add(Transaction(accounts[1], "입금", 10, 150011, LocalDateTime.of(2021, 1, 15, 15, 0, 0)))
    }

val kakaoBankTransactions: Queue<Transaction> = LinkedList<Transaction>()
    .apply {
        add(Transaction(accounts[2], "입금", 1000000, 1000000, LocalDateTime.of(2021, 1, 1, 12, 0, 0)))
        add(Transaction(accounts[2], "입금", 500000, 1500000, LocalDateTime.of(2021, 1, 10, 20, 0, 0)))
        add(Transaction(accounts[2], "입금", 300000, 1800000, LocalDateTime.of(2021, 1, 15, 23, 0, 0)))
        add(Transaction(accounts[2], "입금", 2000000, 3600000, LocalDateTime.of(2021, 1, 30, 15, 0, 0)))
    }

class Week07 {

}
