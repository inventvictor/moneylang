package moneylang.results

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import moneylang.processor.Posting
import moneylang.processor.Transaction
import moneylang.utils.Account

data class TransactionResults(
  val transaction: Transaction?,
  val postings: Map<String, Posting>,
  val accounts: Map<String, Account>,
) {
  fun printPostings() {
    println("Source               Destination                      Currency   Tag                    Amount")
    println("-------------------- -------------------------------- ---------- -------------------- --------")
    postings.forEach { (_, posting) ->
      println(
        String.format(
          "%-20s %-32s %-10s %-20s %10.2f",
          posting.source.id,
          posting.destination.id,
          posting.amount.currency,
          posting.tag,
          posting.amount.value,
        ),
      )
    }
  }

  fun printBalances() {
    println("Account                           Currency       Initial     Balance")
    println("--------------------------------- ----------  ----------  ----------")
    accounts.values.forEach { account ->
      println(String.format("%-33s %-11s %10.2f %10.2f", account.id, account.currency, account.initialBalance, account.balance))
    }
  }

  data class Postings(
    val source: String,
    val destination: String,
    val currency: String,
    val tag: String,
    val amount: Double
  )

  data class Balances(
    val account: String,
    val currency: String,
    val balance: Double,
    val initialBalance: Double
  )

  fun toJsonString() : String {
    val postingsList: List<Postings> = postings.map { (_, value) ->
      Postings(
        value.source.id,
        value.destination.id,
        value.amount.currency,
        value.tag,
        value.amount.value.toDouble()
      )
    }

    val balancesList: List<Balances> = accounts.map { (_, value) ->
      Balances(
        value.id,
        value.currency,
        value.balance.toDouble(),
        value.initialBalance.toDouble()
      )
    }

    return jacksonObjectMapper().writeValueAsString(
      mutableMapOf(
        Pair("postings", postingsList),
        Pair("balances", balancesList)
      )
    )
  }
}
