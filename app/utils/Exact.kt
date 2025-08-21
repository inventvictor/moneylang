package moneylang.utils

import java.math.BigDecimal

class Exact(val value: BigDecimal) {
  fun plus(amount: Number, condition: Boolean = true): ExactAdd {
    if (!condition) {
      return ExactAdd(this, BigDecimal.ZERO)
    }
    return when(amount) {
      is Double -> ExactAdd(this, BigDecimal(amount))
      is Int -> ExactAdd(this, BigDecimal(amount))
      is Long -> ExactAdd(this, BigDecimal(amount))
      else -> throw IllegalArgumentException("Type mismatch: $amount (expected Double, Int or Long)")
    }
  }

  fun minus(amount: Number, condition: Boolean = true): ExactMinus {
    if (!condition) {
      return ExactMinus(this, BigDecimal.ZERO)
    }
    return when(amount) {
      is Double -> ExactMinus(this, BigDecimal(amount))
      is Int -> ExactMinus(this, BigDecimal(amount))
      is Long -> ExactMinus(this, BigDecimal(amount))
      else -> throw IllegalArgumentException("Type mismatch: $amount (expected Double, Int or Long)")
    }
  }

  fun times(amount: Number, condition: Boolean = true): ExactTimes {
    if (!condition) {
      return ExactTimes(this, BigDecimal.ZERO)
    }
    return when(amount) {
      is Double -> ExactTimes(this, BigDecimal(amount))
      is Int -> ExactTimes(this, BigDecimal(amount))
      is Long -> ExactTimes(this, BigDecimal(amount))
      else -> throw IllegalArgumentException("Type mismatch: $amount (expected Double, Int or Long)")
    }
  }

  fun div(amount: Number, condition: Boolean = true): ExactDiv {
    if (!condition) {
      return ExactDiv(this, BigDecimal.ZERO)
    }
    return when(amount) {
      is Double -> ExactDiv(this, BigDecimal(amount))
      is Int -> ExactDiv(this, BigDecimal(amount))
      is Long -> ExactDiv(this, BigDecimal(amount))
      else -> throw IllegalArgumentException("Type mismatch: $amount (expected Double, Int or Long)")
    }
  }
}
