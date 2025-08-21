package moneylang.processor

import moneylang.utils.Account
import moneylang.utils.CurrencyAmount

data class Transaction(
  val amount: CurrencyAmount,
  val source: Account,
  val destination: Destination,
)
