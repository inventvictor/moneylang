package moneylang.utils

import java.math.BigDecimal

fun atleast(amount: Number): MinAmount {
  return when (amount) {
    is Double -> MinAmount(BigDecimal(amount))
    is Int -> MinAmount(BigDecimal(amount))
    is Long -> MinAmount(BigDecimal(amount))
    else -> throw IllegalArgumentException("Type mismatch: $amount (expected Double, Int or Long)")
  }
}

class MinAmount(val amount: BigDecimal)
