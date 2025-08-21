package moneylang.utils

import java.math.BigDecimal

fun upto(amount: Number): MaxAmount {
  return when (amount) {
    is Double -> MaxAmount(BigDecimal(amount))
    is Int -> MaxAmount(BigDecimal(amount))
    is Long -> MaxAmount(BigDecimal(amount))
    else -> throw IllegalArgumentException("Type mismatch: $amount (expected Double, Int or Long)")
  }
}

class MaxAmount(val amount: BigDecimal)
