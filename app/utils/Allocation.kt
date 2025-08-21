package moneylang.utils

import moneylang.processor.Destination
import java.math.BigDecimal

sealed class Allocation {
  data object Source : Allocation()

  data class PercentageCap(
    val percent: Percentage,
    val upper: BigDecimal,
    val lower: BigDecimal,
    val destination: Destination,
  ) : Allocation()
  data class PercentageAdd(
    val percent: Percentage,
    val value: BigDecimal,
    val destination: Destination,
  ) : Allocation()
  data class PercentageMinus(
    val percent: Percentage,
    val value: BigDecimal,
    val destination: Destination,
  ) : Allocation()
  data class PercentageTimes(
    val percent: Percentage,
    val value: BigDecimal,
    val destination: Destination,
  ) : Allocation()
  data class PercentageDiv(
    val percent: Percentage,
    val value: BigDecimal,
    val destination: Destination,
  ) : Allocation()

  data class ExactAdd(
    val exact: Exact,
    val value: BigDecimal,
    val destination: Destination,
  ) : Allocation()
  data class ExactMinus(
    val exact: Exact,
    val value: BigDecimal,
    val destination: Destination,
  ) : Allocation()
  data class ExactTimes(
    val exact: Exact,
    val value: BigDecimal,
    val destination: Destination,
  ) : Allocation()
  data class ExactDiv(
    val exact: Exact,
    val value: BigDecimal,
    val destination: Destination,
  ) : Allocation()

  data class Percentage(val percent: BigDecimal, val destination: Destination) : Allocation()

  data class Exact(val exact: BigDecimal, val destination: Destination) : Allocation()

  data class MaximumAmount(val amount: BigDecimal, val destination: Destination) : Allocation()

  data class MinimumAmount(val amount: BigDecimal, val destination: Destination) : Allocation()

  data class Remainder(val destination: Destination) : Allocation()
}
