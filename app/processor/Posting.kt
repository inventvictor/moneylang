package moneylang.processor

import moneylang.utils.Account
import moneylang.utils.CurrencyAmount

data class Posting(
  val source: Account,
  val destination: Account,
  val amount: CurrencyAmount,
  val tag: String,
) {
  init {
    require(source.currency == amount.currency) {
      "You cannot make a posting of amount '${amount.value}' with currency '${amount.currency}' from account '${source.id}')."
    }

    require(destination.currency == amount.currency) {
      "You cannot make a posting of amount '${amount.value}' with currency '${amount.currency}' to account '${destination.id}'."
    }
  }
}
