package com.example.kotlinstudy

import java.time.LocalDateTime

class Week07 {
    data class Account(val accountNumber: String)

    data class Transaction(
        val account: Account,
        val transactionType: String,
        val amount: Int,
        val balance: Int,
        val transactedAt: LocalDateTime
    )
}
