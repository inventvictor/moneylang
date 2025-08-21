package moneylang.processor

import moneylang.utils.Account
import moneylang.utils.Allocation
import moneylang.utils.AppliedTag
import moneylang.utils.CurrencyAmount
import java.math.BigDecimal

class TransactionProcessor {
  fun process(
    transaction: Transaction,
    appliedTags: MutableMap<String, AppliedTag>,
  ): Map<String, Posting> {
    val postingsMap = linkedMapOf<String, Posting>()
    val amount = BigDecimal.valueOf(transaction.amount.getValueAsDouble())
    val currency = transaction.amount.currency

    if (transaction.source.currency != currency) {
      throw IllegalArgumentException("Account '${transaction.source.id}' does not have a '$currency' balance.")
    }

    if (!transaction.source.allowOverdraft && amount > transaction.source.balance) {
      throw IllegalArgumentException("Account '${transaction.source.id}' with balance '${transaction.source.balance}' is lower than transaction amount '$amount' and overdraft is not allowed.")
    }

    processDestination(
      source = transaction.source,
      destination = transaction.destination,
      totalAmount = amount,
      currency = currency,
      postingsMap = postingsMap,
      pathPrefix = "",
      appliedTags = appliedTags,
    )

    return postingsMap
  }

  private fun processDestination(
    source: Account,
    destination: Destination,
    totalAmount: BigDecimal,
    currency: String,
    postingsMap: MutableMap<String, Posting>,
    pathPrefix: String,
    appliedTags: MutableMap<String, AppliedTag>,
  ): BigDecimal {
    var remainingAmount = totalAmount

    when (destination) {
      is Destination.Account -> {
        val key =
          if (pathPrefix.isEmpty()) destination.account.id else "$pathPrefix.${destination.account.id}"
        postingsMap[key] =
          Posting(source, destination.account, CurrencyAmount(currency, totalAmount), destination.tag)
        remainingAmount = BigDecimal.ZERO
      }

      is Destination.Split -> {
        val exactAddAllocations =
          destination.allocations.filterIsInstance<Allocation.ExactAdd>()
        val exactMinusAllocations =
          destination.allocations.filterIsInstance<Allocation.ExactMinus>()
        val exactTimesAllocations =
          destination.allocations.filterIsInstance<Allocation.ExactTimes>()
        val exactDivAllocations =
          destination.allocations.filterIsInstance<Allocation.ExactDiv>()
        val percentageCapAllocations =
          destination.allocations.filterIsInstance<Allocation.PercentageCap>()
        val percentageAddAllocations =
          destination.allocations.filterIsInstance<Allocation.PercentageAdd>()
        val percentageMinusAllocations =
          destination.allocations.filterIsInstance<Allocation.PercentageMinus>()
        val percentageTimesAllocations =
          destination.allocations.filterIsInstance<Allocation.PercentageTimes>()
        val percentageDivAllocations =
          destination.allocations.filterIsInstance<Allocation.PercentageDiv>()
        val percentageAllocations =
          destination.allocations.filterIsInstance<Allocation.Percentage>()
        val exactAllocations =
          destination.allocations.filterIsInstance<Allocation.Exact>()
        val maxAmountAllocations =
          destination.allocations.filterIsInstance<Allocation.MaximumAmount>()
        val minAmountAllocations =
          destination.allocations.filterIsInstance<Allocation.MinimumAmount>()
        val remainderAllocations = destination.allocations.filterIsInstance<Allocation.Remainder>()
        val sourceAllocations = destination.allocations.filterIsInstance<Allocation.Source>()

        percentageCapAllocations.forEachIndexed { index, allocation ->
          when (allocation.destination) {
            is Destination.Account -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }

            is Destination.Split -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }
          }

          when (val allocationDestination = allocation.destination) {
            is Destination.Account -> {
              val percentDecimal = allocation.percent.percent.divide(BigDecimal(100))
              val allocationAmount = totalAmount.multiply(percentDecimal).min(allocation.upper).max(allocation.lower)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "pC$index" else "$pathPrefix.pC$index"
              postingsMap[currentPath] = Posting(
                source, allocationDestination.account, CurrencyAmount(currency, allocationAmount),
                allocationDestination.tag.takeIf { it.isNotEmpty() } ?: destination.tag,
              )
            }

            is Destination.Split -> {
              val percentDecimal = allocation.percent.percent.divide(BigDecimal(100))
              val allocationAmount = totalAmount.multiply(percentDecimal).min(allocation.upper).max(allocation.lower)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "pC$index" else "$pathPrefix.pC$index"
              processDestination(
                source,
                allocationDestination,
                allocationAmount,
                currency,
                postingsMap,
                currentPath,
                appliedTags,
              )
            }
          }
        }

        percentageAddAllocations.forEachIndexed { index, allocation ->

          when (allocation.destination) {
            is Destination.Account -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }

            is Destination.Split -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }
          }

          when (val allocationDestination = allocation.destination) {
            is Destination.Account -> {
              val percentDecimal = allocation.percent.percent.divide(BigDecimal(100))
              val allocationAmount = totalAmount.multiply(percentDecimal).add(allocation.value)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "pA$index" else "$pathPrefix.pA$index"
              postingsMap[currentPath] = Posting(
                source, allocationDestination.account, CurrencyAmount(currency, allocationAmount),
                allocationDestination.tag.takeIf { it.isNotEmpty() } ?: destination.tag,
              )
            }

            is Destination.Split -> {
              val percentDecimal = allocation.percent.percent.divide(BigDecimal(100))
              val allocationAmount = totalAmount.multiply(percentDecimal).add(allocation.value)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "pA$index" else "$pathPrefix.pA$index"
              processDestination(
                source,
                allocationDestination,
                allocationAmount,
                currency,
                postingsMap,
                currentPath,
                appliedTags,
              )
            }
          }
        }

        percentageMinusAllocations.forEachIndexed { index, allocation ->

          when (allocation.destination) {
            is Destination.Account -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }

            is Destination.Split -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }
          }

          when (val allocationDestination = allocation.destination) {
            is Destination.Account -> {
              val percentDecimal = allocation.percent.percent.divide(BigDecimal(100))
              val allocationAmount = totalAmount.multiply(percentDecimal).minus(allocation.value)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "pM$index" else "$pathPrefix.pM$index"
              postingsMap[currentPath] = Posting(
                source, allocationDestination.account, CurrencyAmount(currency, allocationAmount),
                allocationDestination.tag.takeIf { it.isNotEmpty() } ?: destination.tag,
              )
            }

            is Destination.Split -> {
              val percentDecimal = allocation.percent.percent.divide(BigDecimal(100))
              val allocationAmount = totalAmount.multiply(percentDecimal).minus(allocation.value)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "pM$index" else "$pathPrefix.pM$index"
              processDestination(
                source,
                allocationDestination,
                allocationAmount,
                currency,
                postingsMap,
                currentPath,
                appliedTags,
              )
            }
          }
        }

        percentageTimesAllocations.forEachIndexed { index, allocation ->

          when (allocation.destination) {
            is Destination.Account -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }

            is Destination.Split -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }
          }

          when (val allocationDestination = allocation.destination) {
            is Destination.Account -> {
              val percentDecimal = allocation.percent.percent.divide(BigDecimal(100))
              val allocationAmount = totalAmount.multiply(percentDecimal).times(allocation.value)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "pT$index" else "$pathPrefix.pT$index"
              postingsMap[currentPath] = Posting(
                source, allocationDestination.account, CurrencyAmount(currency, allocationAmount),
                allocationDestination.tag.takeIf { it.isNotEmpty() } ?: destination.tag,
              )
            }

            is Destination.Split -> {
              val percentDecimal = allocation.percent.percent.divide(BigDecimal(100))
              val allocationAmount = totalAmount.multiply(percentDecimal).times(allocation.value)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "pT$index" else "$pathPrefix.pT$index"
              processDestination(
                source,
                allocationDestination,
                allocationAmount,
                currency,
                postingsMap,
                currentPath,
                appliedTags,
              )
            }
          }
        }

        percentageDivAllocations.forEachIndexed { index, allocation ->

          when (allocation.destination) {
            is Destination.Account -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }

            is Destination.Split -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }
          }

          when (val allocationDestination = allocation.destination) {
            is Destination.Account -> {
              val percentDecimal = allocation.percent.percent.divide(BigDecimal(100))
              val allocationAmount = totalAmount.multiply(percentDecimal).div(allocation.value)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "pD$index" else "$pathPrefix.pD$index"
              postingsMap[currentPath] = Posting(
                source, allocationDestination.account, CurrencyAmount(currency, allocationAmount),
                allocationDestination.tag.takeIf { it.isNotEmpty() } ?: destination.tag,
              )
            }

            is Destination.Split -> {
              val percentDecimal = allocation.percent.percent.divide(BigDecimal(100))
              val allocationAmount = totalAmount.multiply(percentDecimal).div(allocation.value)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "pD$index" else "$pathPrefix.pD$index"
              processDestination(
                source,
                allocationDestination,
                allocationAmount,
                currency,
                postingsMap,
                currentPath,
                appliedTags,
              )
            }
          }
        }

        exactAddAllocations.forEachIndexed { index, allocation ->

          when (allocation.destination) {
            is Destination.Account -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }

            is Destination.Split -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }
          }

          when (val allocationDestination = allocation.destination) {
            is Destination.Account -> {
              val allocationAmount = allocation.exact.exact.add(allocation.value)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "eA$index" else "$pathPrefix.eA$index"
              postingsMap[currentPath] = Posting(
                source, allocationDestination.account, CurrencyAmount(currency, allocationAmount),
                allocationDestination.tag.takeIf { it.isNotEmpty() } ?: destination.tag,
              )
            }

            is Destination.Split -> {
              val allocationAmount = allocation.exact.exact.add(allocation.value)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "eA$index" else "$pathPrefix.eA$index"
              processDestination(
                source,
                allocationDestination,
                allocationAmount,
                currency,
                postingsMap,
                currentPath,
                appliedTags,
              )
            }
          }
        }

        exactMinusAllocations.forEachIndexed { index, allocation ->

          when (allocation.destination) {
            is Destination.Account -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }

            is Destination.Split -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }
          }

          when (val allocationDestination = allocation.destination) {
            is Destination.Account -> {
              val allocationAmount = allocation.exact.exact.minus(allocation.value)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "eM$index" else "$pathPrefix.eM$index"
              postingsMap[currentPath] = Posting(
                source, allocationDestination.account, CurrencyAmount(currency, allocationAmount),
                allocationDestination.tag.takeIf { it.isNotEmpty() } ?: destination.tag,
              )
            }

            is Destination.Split -> {
              val allocationAmount = allocation.exact.exact.minus(allocation.value)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "eM$index" else "$pathPrefix.eM$index"
              processDestination(
                source,
                allocationDestination,
                allocationAmount,
                currency,
                postingsMap,
                currentPath,
                appliedTags,
              )
            }
          }
        }

        exactTimesAllocations.forEachIndexed { index, allocation ->

          when (allocation.destination) {
            is Destination.Account -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }

            is Destination.Split -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }
          }

          when (val allocationDestination = allocation.destination) {
            is Destination.Account -> {
              val allocationAmount = allocation.exact.exact.times(allocation.value)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "eT$index" else "$pathPrefix.eT$index"
              postingsMap[currentPath] = Posting(
                source, allocationDestination.account, CurrencyAmount(currency, allocationAmount),
                allocationDestination.tag.takeIf { it.isNotEmpty() } ?: destination.tag,
              )
            }

            is Destination.Split -> {
              val allocationAmount = allocation.exact.exact.times(allocation.value)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "eT$index" else "$pathPrefix.eT$index"
              processDestination(
                source,
                allocationDestination,
                allocationAmount,
                currency,
                postingsMap,
                currentPath,
                appliedTags,
              )
            }
          }
        }

        exactDivAllocations.forEachIndexed { index, allocation ->

          when (allocation.destination) {
            is Destination.Account -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }

            is Destination.Split -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }
          }

          when (val allocationDestination = allocation.destination) {
            is Destination.Account -> {
              val allocationAmount = allocation.exact.exact.div(allocation.value)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "eD$index" else "$pathPrefix.eD$index"
              postingsMap[currentPath] = Posting(
                source, allocationDestination.account, CurrencyAmount(currency, allocationAmount),
                allocationDestination.tag.takeIf { it.isNotEmpty() } ?: destination.tag,
              )
            }

            is Destination.Split -> {
              val allocationAmount = allocation.exact.exact.div(allocation.value)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "eD$index" else "$pathPrefix.eD$index"
              processDestination(
                source,
                allocationDestination,
                allocationAmount,
                currency,
                postingsMap,
                currentPath,
                appliedTags,
              )
            }
          }
        }

        percentageAllocations.forEachIndexed { index, allocation ->

          when (allocation.destination) {
            is Destination.Account -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }

            is Destination.Split -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }
          }

          when (val allocationDestination = allocation.destination) {
            is Destination.Account -> {
              val percentDecimal = allocation.percent.divide(BigDecimal(100))
              val allocationAmount = totalAmount.multiply(percentDecimal)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "p$index" else "$pathPrefix.p$index"
              postingsMap[currentPath] = Posting(
                source, allocationDestination.account, CurrencyAmount(currency, allocationAmount),
                allocationDestination.tag.takeIf { it.isNotEmpty() } ?: destination.tag,
              )
            }

            is Destination.Split -> {
              val percentDecimal = allocation.percent.divide(BigDecimal(100))
              val allocationAmount = totalAmount.multiply(percentDecimal)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "p$index" else "$pathPrefix.p$index"
              processDestination(
                source,
                allocationDestination,
                allocationAmount,
                currency,
                postingsMap,
                currentPath,
                appliedTags,
              )
            }
          }
        }

        exactAllocations.forEachIndexed { index, allocation ->

          when (allocation.destination) {
            is Destination.Account -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }

            is Destination.Split -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }
          }

          when (val allocationDestination = allocation.destination) {
            is Destination.Account -> {
              val allocationAmount = allocation.exact
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "e$index" else "$pathPrefix.e$index"
              postingsMap[currentPath] = Posting(
                source, allocationDestination.account, CurrencyAmount(currency, allocationAmount),
                allocationDestination.tag.takeIf { it.isNotEmpty() } ?: destination.tag,
              )
            }

            is Destination.Split -> {
              val allocationAmount = totalAmount
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "e$index" else "$pathPrefix.e$index"
              processDestination(
                source,
                allocationDestination,
                allocationAmount,
                currency,
                postingsMap,
                currentPath,
                appliedTags,
              )
            }
          }
        }

        maxAmountAllocations.forEachIndexed { index, allocation ->

          when (allocation.destination) {
            is Destination.Account -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }

            is Destination.Split -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }
          }

          when (val allocationDestination = allocation.destination) {
            is Destination.Account -> {
              val maxValue = allocation.amount
              val allocationAmount = if (remainingAmount < maxValue) remainingAmount else remainingAmount.min(maxValue)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "max$index" else "$pathPrefix.max$index"
              postingsMap[currentPath] = Posting(
                source, allocationDestination.account, CurrencyAmount(currency, allocationAmount),
                allocationDestination.tag.takeIf { it.isNotEmpty() } ?: destination.tag,
              )
            }

            is Destination.Split -> {
              val maxValue = allocation.amount
              val allocationAmount = if (remainingAmount < maxValue) remainingAmount else remainingAmount.min(maxValue)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "max$index" else "$pathPrefix.max$index"
              processDestination(
                source,
                allocationDestination,
                allocationAmount,
                currency,
                postingsMap,
                currentPath,
                appliedTags,
              )
            }
          }
        }

        minAmountAllocations.forEachIndexed { index, allocation ->

          when (allocation.destination) {
            is Destination.Account -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }

            is Destination.Split -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }
          }

          when (val allocationDestination = allocation.destination) {
            is Destination.Account -> {
              println(allocation.amount)
              println(remainingAmount)
              val minValue = allocation.amount
              val allocationAmount = if (remainingAmount < minValue) remainingAmount else remainingAmount.max(minValue)
              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "min$index" else "$pathPrefix.min$index"
              postingsMap[currentPath] = Posting(
                source, allocationDestination.account, CurrencyAmount(currency, allocationAmount),
                allocationDestination.tag.takeIf { it.isNotEmpty() } ?: destination.tag,
              )
            }

            is Destination.Split -> {
              val minValue = allocation.amount
              val allocationAmount = if (remainingAmount < minValue) remainingAmount else remainingAmount.max(minValue)

              remainingAmount = remainingAmount.subtract(allocationAmount)
              val currentPath = if (pathPrefix.isEmpty()) "min$index" else "$pathPrefix.min$index"
              processDestination(
                source,
                allocationDestination,
                allocationAmount,
                currency,
                postingsMap,
                currentPath,
                appliedTags,
              )
            }
          }
        }

        remainderAllocations.forEachIndexed { index, allocation ->

          when (allocation.destination) {
            is Destination.Account -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }

            is Destination.Split -> {
              val tag = allocation.destination.tag.takeIf { it.isNotEmpty() } ?: destination.tag
              if (appliedTags[tag]?.condition == false) {
                return@forEachIndexed
              }
            }
          }

          if (remainingAmount > BigDecimal.ZERO) {
            val currentPath = if (pathPrefix.isEmpty()) "r$index" else "$pathPrefix.r$index"

            when (val allocationDestination = allocation.destination) {
              is Destination.Account -> {
                postingsMap[currentPath] = Posting(
                  source, allocationDestination.account, CurrencyAmount(currency, remainingAmount),
                  allocationDestination.tag.takeIf { it.isNotEmpty() } ?: destination.tag,
                )
                remainingAmount = BigDecimal.ZERO
              }

              is Destination.Split -> {
                processDestination(
                  source,
                  allocationDestination,
                  remainingAmount,
                  currency,
                  postingsMap,
                  currentPath,
                  appliedTags,
                )
                remainingAmount = BigDecimal.ZERO
              }
            }
          }
        }

        sourceAllocations.forEachIndexed { index, _ ->

          if (appliedTags[destination.tag]?.condition == false) {
            return@forEachIndexed
          }

          if (remainingAmount > BigDecimal.ZERO) {
            val currentPath = if (pathPrefix.isEmpty()) "s$index" else "$pathPrefix.s$index"
            postingsMap[currentPath] =
              Posting(source, source, CurrencyAmount(currency, remainingAmount), destination.tag)
            remainingAmount = BigDecimal.ZERO
          }
        }
      }
    }

    return remainingAmount
  }
}
