package moneylang.utils

import java.math.BigDecimal

class Percentage(val value: BigDecimal) {
  fun cap(upper: Number, lower: Number): PercentageCap {
    return when(upper) {
      is Double -> PercentageCap(this, BigDecimal(upper), when(lower) {
        is Double -> BigDecimal(lower)
        is Int -> BigDecimal(lower)
        is Long -> BigDecimal(lower)
        else -> throw IllegalArgumentException("Type mismatch: $lower (expected Double, Int or Long)")
      })
      is Int -> PercentageCap(this, BigDecimal(upper), when(lower) {
        is Double -> BigDecimal(lower)
        is Int -> BigDecimal(lower)
        is Long -> BigDecimal(lower)
        else -> throw IllegalArgumentException("Type mismatch: $lower (expected Double, Int or Long)")
      })
      is Long -> PercentageCap(this, BigDecimal(upper), when(lower) {
        is Double -> BigDecimal(lower)
        is Int -> BigDecimal(lower)
        is Long -> BigDecimal(lower)
        else -> throw IllegalArgumentException("Type mismatch: $lower (expected Double, Int or Long)")
      })
      else -> throw IllegalArgumentException("Type mismatch: $upper (expected Double, Int or Long)")
    }
  }

  fun plus(amount: Number, condition: Boolean = true): PercentageAdd {
    if (!condition) {
      return PercentageAdd(this, BigDecimal.ZERO, Exact(BigDecimal.ZERO))
    }
    return when(amount) {
      is Double -> PercentageAdd(this, BigDecimal(amount), Exact(BigDecimal(amount)))
      is Int -> PercentageAdd(this, BigDecimal(amount), Exact(BigDecimal(amount)))
      is Long -> PercentageAdd(this, BigDecimal(amount), Exact(BigDecimal(amount)))
      else -> throw IllegalArgumentException("Type mismatch: $amount (expected Double, Int or Long)")
    }
  }

  fun minus(amount: Number, condition: Boolean = true): PercentageMinus {
    if (!condition) {
      return PercentageMinus(this, BigDecimal.ZERO, Exact(BigDecimal.ZERO))
    }
    return when(amount) {
      is Double -> PercentageMinus(this, BigDecimal(amount), Exact(BigDecimal(amount)))
      is Int -> PercentageMinus(this, BigDecimal(amount), Exact(BigDecimal(amount)))
      is Long -> PercentageMinus(this, BigDecimal(amount), Exact(BigDecimal(amount)))
      else -> throw IllegalArgumentException("Type mismatch: $amount (expected Double, Int or Long)")
    }
  }

  fun times(amount: Number, condition: Boolean = true): PercentageTimes {
    if (!condition) {
      return PercentageTimes(this, BigDecimal.ZERO, Exact(BigDecimal.ZERO))
    }
    return when(amount) {
      is Double -> PercentageTimes(this, BigDecimal(amount), Exact(BigDecimal(amount)))
      is Int -> PercentageTimes(this, BigDecimal(amount), Exact(BigDecimal(amount)))
      is Long -> PercentageTimes(this, BigDecimal(amount), Exact(BigDecimal(amount)))
      else -> throw IllegalArgumentException("Type mismatch: $amount (expected Double, Int or Long)")
    }
  }

  fun div(amount: Number, condition: Boolean = true): PercentageDiv {
    if (!condition) {
      return PercentageDiv(this, BigDecimal.ZERO, Exact(BigDecimal.ZERO))
    }
    return when(amount) {
      is Double -> PercentageDiv(this, BigDecimal(amount), Exact(BigDecimal(amount)))
      is Int -> PercentageDiv(this, BigDecimal(amount), Exact(BigDecimal(amount)))
      is Long -> PercentageDiv(this, BigDecimal(amount), Exact(BigDecimal(amount)))
      else -> throw IllegalArgumentException("Type mismatch: $amount (expected Double, Int or Long)")
    }
  }
}
