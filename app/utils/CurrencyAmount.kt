package moneylang.utils

import java.math.BigDecimal

open class CurrencyAmount(val currency: String, val value: Number) {
  fun getValueAsDouble() : Double {
    return when (value) {
      is Double -> BigDecimal(value).toDouble()
      is Int -> BigDecimal(value).toDouble()
      is Long -> BigDecimal(value).toDouble()
      else -> throw IllegalArgumentException("Type mismatch: $value (expected Double, Int or Long)")
    }
  }
}

class USD(value: Number) : CurrencyAmount(currency = "USD", value = value)

class NGN(value: Number) : CurrencyAmount(currency = "NGN", value = value)

class GBP(value: Number) : CurrencyAmount(currency = "GBP", value = value)

class EUR(value: Number) : CurrencyAmount(currency = "EUR", value = value)

class JPY(value: Number) : CurrencyAmount(currency = "JPY", value = value)

class CAD(value: Number) : CurrencyAmount(currency = "CAD", value = value)

class AUD(value: Number) : CurrencyAmount(currency = "AUD", value = value)

class CHF(value: Number) : CurrencyAmount(currency = "CHF", value = value)

class CNY(value: Number) : CurrencyAmount(currency = "CNY", value = value)

class INR(value: Number) : CurrencyAmount(currency = "INR", value = value)
