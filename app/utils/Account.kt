package moneylang.utils

import java.math.BigDecimal

data class Account(
  val id: String,
  var balance: BigDecimal,
  var initialBalance: BigDecimal = balance,
  val currency: String,
  val allowOverdraft: Boolean = false,
  val overdraftLimit: BigDecimal = 0.0.toBigDecimal(),
)
