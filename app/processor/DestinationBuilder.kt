package moneylang.processor

import moneylang.utils.*
import moneylang.dsl.MoneyLangDsl
import java.math.BigDecimal

@MoneyLangDsl
class DestinationBuilder(val amount: BigDecimal,
                         private val variables: MutableMap<String, Variable>,
                         private val accounts: MutableMap<String, Account>,
                         private val metadata: MutableMap<String, Metadata>) {

  val allocations = mutableListOf<Allocation>()
  var currentTag = ""

  private fun getAccount(path: String): Account {
    return accounts[path] ?: throw IllegalStateException("Account $path does not exist")
  }

  fun varNumber(placeholder: String): Number {
    if (!placeholder.startsWith("%%") || !placeholder.endsWith("%%")) {
      throw IllegalArgumentException("Invalid placeholder: $placeholder. Must start and end with '%%'")
    }
    return when(variables[placeholder]?.data) {
      is Double -> variables[placeholder]?.data as Double
      is Int -> variables[placeholder]?.data as Int
      is Long -> variables[placeholder]?.data as Long
      else -> throw IllegalStateException("Placeholder $placeholder does not exist")
    }
  }

  fun varString(placeholder: String): String {
    if (!placeholder.startsWith("%%") || !placeholder.endsWith("%%")) {
      throw IllegalArgumentException("Invalid placeholder: $placeholder. Must start and end with '%%'")
    }
    return when(variables[placeholder]?.data) {
      is String -> variables[placeholder]?.data as String
      else -> throw IllegalStateException("Placeholder $placeholder does not exist")
    }
  }

  val Number.percent: Percentage
    get() = when (this) {
      is Double -> Percentage(BigDecimal.valueOf(this))
      is Int -> Percentage(BigDecimal(this))
      is Long -> Percentage(BigDecimal(this))
      else -> throw IllegalArgumentException("Type mismatch: $this (expected Double, Int or Long)")
    }

  val Number.exact: Exact
    get() = when (this) {
      is Double -> Exact(BigDecimal.valueOf(this))
      is Int -> Exact(BigDecimal(this))
      is Long -> Exact(BigDecimal(this))
      else -> throw IllegalArgumentException("Type mismatch: $this (expected Double, Int or Long)")
    }

  fun BigDecimal.between(min: Number, max: Number): Boolean {
    return when (min) {
      is Double -> when(max) {
        is Double -> this >= BigDecimal(min) && this <= BigDecimal(max)
        else -> throw IllegalArgumentException("Type mismatch: $max (expected Double)")
      }
      is Int -> when(max) {
        is Int -> this >= BigDecimal(min) && this <= BigDecimal(max)
        else -> throw IllegalArgumentException("Type mismatch: $max (expected Int)")
      }
      is Long -> when(max) {
        is Int -> this >= BigDecimal(min) && this <= BigDecimal(max)
        else -> throw IllegalArgumentException("Type mismatch: $max (expected Long)")
      }
      else -> throw IllegalArgumentException("Type mismatch: $amount (expected Double, Int or Long)")
    }
  }

  fun BigDecimal.isZero(): Boolean {
    return this.compareTo(BigDecimal.ZERO) == 0
  }

  fun BigDecimal.isPositive(): Boolean {
    return this > BigDecimal.ZERO
  }

  fun BigDecimal.isNegative(): Boolean {
    return this < BigDecimal.ZERO
  }

  fun BigDecimal.round(scale: Int = 2): BigDecimal {
    return this.setScale(scale, java.math.RoundingMode.HALF_UP)
  }

  fun BigDecimal.roundUp(scale: Int = 2): BigDecimal {
    return this.setScale(scale, java.math.RoundingMode.CEILING)
  }

  fun BigDecimal.roundDown(scale: Int = 2): BigDecimal {
    return this.setScale(scale, java.math.RoundingMode.FLOOR)
  }

  operator fun BigDecimal.plus(other: Number): BigDecimal {
    return when (other) {
      is Double -> this.add(BigDecimal.valueOf(other))
      is Int -> this.add(BigDecimal(other))
      is Long -> this.add(BigDecimal(other))
      else -> throw IllegalArgumentException("Type mismatch: $other (expected Double, Int or Long)")
    }
  }

  operator fun BigDecimal.minus(other: Number): BigDecimal {
    return when (other) {
      is Double -> this.subtract(BigDecimal.valueOf(other))
      is Int -> this.subtract(BigDecimal(other))
      is Long -> this.subtract(BigDecimal(other))
      else -> throw IllegalArgumentException("Type mismatch: $other (expected Double, Int or Long)")
    }
  }

  operator fun BigDecimal.times(other: Number): BigDecimal {
    return when (other) {
      is Double -> this.multiply(BigDecimal.valueOf(other))
      is Int -> this.multiply(BigDecimal(other))
      is Long -> this.multiply(BigDecimal(other))
      else -> throw IllegalArgumentException("Type mismatch: $other (expected Double, Int or Long)")
    }
  }

  operator fun BigDecimal.div(other: Number): BigDecimal {
    return when (other) {
      is Double -> this.divide(BigDecimal(other), 10, java.math.RoundingMode.HALF_UP)
      is Int -> this.divide(BigDecimal(other), 10, java.math.RoundingMode.HALF_UP)
      is Long -> this.divide(BigDecimal(other), 10, java.math.RoundingMode.HALF_UP)
      else -> throw IllegalArgumentException("Type mismatch: $other (expected Double, Int or Long)")
    }
  }

  fun BigDecimal.max(other: Number): BigDecimal {
    return when (other) {
      is Double -> this.max(BigDecimal(other))
      is Int -> this.max(BigDecimal(other))
      is Long -> this.max(BigDecimal(other))
      else -> throw IllegalArgumentException("Type mismatch: $other (expected Double, Int or Long)")
    }
  }

  fun BigDecimal.min(other: Number): BigDecimal {
    return when (other) {
      is Double -> this.min(BigDecimal(other))
      is Int -> this.min(BigDecimal(other))
      is Long -> this.min(BigDecimal(other))
      else -> throw IllegalArgumentException("Type mismatch: $other (expected Double, Int or Long)")
    }
  }

  fun tag(tag: String, block: DestinationBuilder.() -> Unit) {
    currentTag = tag
    block()
  }

  infix fun PercentageCap.to(path: String) {
    allocations.add(
      Allocation.PercentageCap(
        Allocation.Percentage(
          this.percent.value,
          Destination.Account(getAccount(path), currentTag),
        ),
        this.upper, this.lower, Destination.Account(getAccount(path), currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun PercentageCap.to(block: DestinationBuilder.() -> Unit) {
    val builder = DestinationBuilder(amount, variables, accounts, metadata)
    builder.block()
    allocations.add(
      Allocation.PercentageCap(
        Allocation.Percentage(
          this.percent.value,
          Destination.Split(builder.allocations, currentTag),
        ),
        this.upper, this.lower, Destination.Split(builder.allocations, currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun PercentageAdd.to(path: String) {
    allocations.add(
      Allocation.PercentageAdd(
        Allocation.Percentage(
          this.percent.value,
          Destination.Account(getAccount(path), currentTag),
        ),
        this.value, Destination.Account(getAccount(path), currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun PercentageAdd.to(block: DestinationBuilder.() -> Unit) {
    val builder = DestinationBuilder(amount, variables, accounts, metadata)
    builder.block()
    allocations.add(
      Allocation.PercentageAdd(
        Allocation.Percentage(
          this.percent.value,
          Destination.Split(builder.allocations, currentTag),
        ),
        this.value, Destination.Split(builder.allocations, currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun PercentageMinus.to(path: String) {
    allocations.add(
      Allocation.PercentageMinus(
        Allocation.Percentage(
          this.percent.value,
          Destination.Account(getAccount(path), currentTag),
        ),
        this.value, Destination.Account(getAccount(path), currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun PercentageMinus.to(block: DestinationBuilder.() -> Unit) {
    val builder = DestinationBuilder(amount, variables, accounts, metadata)
    builder.block()
    allocations.add(
      Allocation.PercentageMinus(
        Allocation.Percentage(
          this.percent.value,
          Destination.Split(builder.allocations, currentTag),
        ),
        this.value, Destination.Split(builder.allocations, currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun PercentageTimes.to(path: String) {
    allocations.add(
      Allocation.PercentageTimes(
        Allocation.Percentage(
          this.percent.value,
          Destination.Account(getAccount(path), currentTag),
        ),
        this.value, Destination.Account(getAccount(path), currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun PercentageTimes.to(block: DestinationBuilder.() -> Unit) {
    val builder = DestinationBuilder(amount, variables, accounts, metadata)
    builder.block()
    allocations.add(
      Allocation.PercentageTimes(
        Allocation.Percentage(
          this.percent.value,
          Destination.Split(builder.allocations, currentTag),
        ),
        this.value, Destination.Split(builder.allocations, currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun PercentageDiv.to(path: String) {
    allocations.add(
      Allocation.PercentageDiv(
        Allocation.Percentage(
          this.percent.value,
          Destination.Account(getAccount(path), currentTag),
        ),
        this.value, Destination.Account(getAccount(path), currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun PercentageDiv.to(block: DestinationBuilder.() -> Unit) {
    val builder = DestinationBuilder(amount, variables, accounts, metadata)
    builder.block()
    allocations.add(
      Allocation.PercentageDiv(
        Allocation.Percentage(
          this.percent.value,
          Destination.Split(builder.allocations, currentTag),
        ),
        this.value, Destination.Split(builder.allocations, currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun ExactAdd.to(path: String) {
    allocations.add(
      Allocation.ExactAdd(
        Allocation.Exact(
          this.exact.value,
          Destination.Account(getAccount(path), currentTag),
        ),
        this.value, Destination.Account(getAccount(path), currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun ExactAdd.to(block: DestinationBuilder.() -> Unit) {
    val builder = DestinationBuilder(amount, variables, accounts, metadata)
    builder.block()
    allocations.add(
      Allocation.ExactAdd(
        Allocation.Exact(
          this.exact.value,
          Destination.Split(builder.allocations, currentTag),
        ),
        this.value, Destination.Split(builder.allocations, currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun ExactMinus.to(path: String) {
    allocations.add(
      Allocation.ExactMinus(
        Allocation.Exact(
          this.exact.value,
          Destination.Account(getAccount(path), currentTag),
        ),
        this.value, Destination.Account(getAccount(path), currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun ExactMinus.to(block: DestinationBuilder.() -> Unit) {
    val builder = DestinationBuilder(amount, variables, accounts, metadata)
    builder.block()
    allocations.add(
      Allocation.ExactMinus(
        Allocation.Exact(
          this.exact.value,
          Destination.Split(builder.allocations, currentTag),
        ),
        this.value, Destination.Split(builder.allocations, currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun ExactTimes.to(path: String) {
    allocations.add(
      Allocation.ExactTimes(
        Allocation.Exact(
          this.exact.value,
          Destination.Account(getAccount(path), currentTag),
        ),
        this.value, Destination.Account(getAccount(path), currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun ExactTimes.to(block: DestinationBuilder.() -> Unit) {
    val builder = DestinationBuilder(amount, variables, accounts, metadata)
    builder.block()
    allocations.add(
      Allocation.ExactTimes(
        Allocation.Exact(
          this.exact.value,
          Destination.Split(builder.allocations, currentTag),
        ),
        this.value, Destination.Split(builder.allocations, currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun ExactDiv.to(path: String) {
    allocations.add(
      Allocation.ExactDiv(
        Allocation.Exact(
          this.exact.value,
          Destination.Account(getAccount(path), currentTag),
        ),
        this.value, Destination.Account(getAccount(path), currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun ExactDiv.to(block: DestinationBuilder.() -> Unit) {
    val builder = DestinationBuilder(amount, variables, accounts, metadata)
    builder.block()
    allocations.add(
      Allocation.ExactDiv(
        Allocation.Exact(
          this.exact.value,
          Destination.Split(builder.allocations, currentTag),
        ),
        this.value, Destination.Split(builder.allocations, currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun Percentage.to(path: String) {
    allocations.add(Allocation.Percentage(value, Destination.Account(getAccount(path), currentTag)))
    currentTag = ""
  }

  infix fun Percentage.to(block: DestinationBuilder.() -> Unit) {
    val builder = DestinationBuilder(amount, variables, accounts, metadata)
    builder.block()
    allocations.add(
      Allocation.Percentage(
        value,
        Destination.Split(builder.allocations, currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun Exact.to(path: String) {
    allocations.add(Allocation.Exact(value, Destination.Account(getAccount(path), currentTag)))
    currentTag = ""
  }

  infix fun Exact.to(block: DestinationBuilder.() -> Unit) {
    val builder = DestinationBuilder(amount, variables, accounts, metadata)
    builder.block()
    allocations.add(
      Allocation.Exact(
        value,
        Destination.Split(builder.allocations, currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun remaining.to(path: String) {
    allocations.add(Allocation.Remainder(Destination.Account(getAccount(path), currentTag)))
    currentTag = ""
  }

  val source = Unit
  infix fun remaining.to(source: Unit) {
    allocations.add(Allocation.Source)
  }

  infix fun MaxAmount.to(path: String) {
    allocations.add(Allocation.MaximumAmount(amount, Destination.Account(getAccount(path), currentTag)))
    currentTag = ""
  }

  infix fun MinAmount.to(path: String) {
    allocations.add(Allocation.MinimumAmount(amount, Destination.Account(getAccount(path), currentTag)))
    currentTag = ""
  }

  infix fun remaining.to(block: DestinationBuilder.() -> Unit) {
    val builder = DestinationBuilder(amount, variables, accounts, metadata)
    builder.block()
    allocations.add(Allocation.Remainder(Destination.Split(builder.allocations, currentTag)))
    currentTag = ""
  }

  infix fun MaxAmount.to(block: DestinationBuilder.() -> Unit) {
    val builder = DestinationBuilder(amount, variables, accounts, metadata)
    builder.block()
    allocations.add(
      Allocation.MaximumAmount(
        amount,
        Destination.Split(builder.allocations, currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun MinAmount.to(block: DestinationBuilder.() -> Unit) {
    val builder = DestinationBuilder(amount, variables, accounts, metadata)
    builder.block()
    allocations.add(
      Allocation.MinimumAmount(
        amount,
        Destination.Split(builder.allocations, currentTag),
      ),
    )
    currentTag = ""
  }

  infix fun Any.gt(value: Int): Boolean {
    return when (this) {
      is Int -> this > value
      is Long -> this > value
      is Double -> this > value
      is BigDecimal -> this > BigDecimal(value)
      is Number -> this.toDouble() > value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Int (${value}) using >")
    }
  }

  infix fun Any.gt(value: Long): Boolean {
    return when (this) {
      is Int -> this > value
      is Long -> this > value
      is Double -> this > value
      is BigDecimal -> this > BigDecimal(value)
      is Number -> this.toLong() > value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Long (${value}) using >")
    }
  }

  infix fun Any.gt(value: Double): Boolean {
    return when (this) {
      is Int -> this > value
      is Long -> this > value
      is Double -> this > value
      is BigDecimal -> this.toDouble() > value
      is Number -> this.toDouble() > value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Double (${value}) using >")
    }
  }

  infix fun Any.gt(value: BigDecimal): Boolean {
    return when (this) {
      is Int -> BigDecimal(this) > value
      is Long -> BigDecimal(this) > value
      is Double -> BigDecimal(this) > value
      is BigDecimal -> this > value
      is Number -> BigDecimal(this.toString()) > value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with BigDecimal (${value}) using >")
    }
  }

  infix fun Any.lt(value: Int): Boolean {
    return when (this) {
      is Int -> this < value
      is Long -> this < value
      is Double -> this < value
      is BigDecimal -> this < BigDecimal(value)
      is Number -> this.toDouble() < value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Int (${value}) using <")
    }
  }

  infix fun Any.lt(value: Long): Boolean {
    return when (this) {
      is Int -> this < value
      is Long -> this < value
      is Double -> this < value
      is BigDecimal -> this < BigDecimal(value)
      is Number -> this.toLong() < value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Long (${value}) using <")
    }
  }

  infix fun Any.lt(value: Double): Boolean {
    return when (this) {
      is Int -> this < value
      is Long -> this < value
      is Double -> this < value
      is BigDecimal -> this.toDouble() < value
      is Number -> this.toDouble() < value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Double (${value}) using <")
    }
  }

  infix fun Any.lt(value: BigDecimal): Boolean {
    return when (this) {
      is Int -> BigDecimal(this) < value
      is Long -> BigDecimal(this) < value
      is Double -> BigDecimal(this) < value
      is BigDecimal -> this < value
      is Number -> BigDecimal(this.toString()) < value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with BigDecimal (${value}) using <")
    }
  }

  infix fun Any.gte(value: Int): Boolean {
    return when (this) {
      is Int -> this >= value
      is Long -> this >= value
      is Double -> this >= value
      is BigDecimal -> this >= BigDecimal(value)
      is Number -> this.toDouble() >= value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Int (${value}) using >=")
    }
  }

  infix fun Any.gte(value: Long): Boolean {
    return when (this) {
      is Int -> this >= value
      is Long -> this >= value
      is Double -> this >= value
      is BigDecimal -> this >= BigDecimal(value)
      is Number -> this.toLong() >= value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Long (${value}) using >=")
    }
  }

  infix fun Any.gte(value: Double): Boolean {
    return when (this) {
      is Int -> this >= value
      is Long -> this >= value
      is Double -> this >= value
      is BigDecimal -> this.toDouble() >= value
      is Number -> this.toDouble() >= value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Double (${value}) using >=")
    }
  }

  infix fun Any.gte(value: BigDecimal): Boolean {
    return when (this) {
      is Int -> BigDecimal(this) >= value
      is Long -> BigDecimal(this) >= value
      is Double -> BigDecimal(this) >= value
      is BigDecimal -> this >= value
      is Number -> BigDecimal(this.toString()) >= value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with BigDecimal (${value}) using >=")
    }
  }

  infix fun Any.lte(value: Int): Boolean {
    return when (this) {
      is Int -> this <= value
      is Long -> this <= value
      is Double -> this <= value
      is BigDecimal -> this <= BigDecimal(value)
      is Number -> this.toDouble() <= value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Int (${value}) using <=")
    }
  }

  infix fun Any.lte(value: Long): Boolean {
    return when (this) {
      is Int -> this <= value
      is Long -> this <= value
      is Double -> this <= value
      is BigDecimal -> this <= BigDecimal(value)
      is Number -> this.toLong() <= value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Long (${value}) using <=")
    }
  }

  infix fun Any.lte(value: Double): Boolean {
    return when (this) {
      is Int -> this <= value
      is Long -> this <= value
      is Double -> this <= value
      is BigDecimal -> this.toDouble() <= value
      is Number -> this.toDouble() <= value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Double (${value}) using <=")
    }
  }

  infix fun Any.lte(value: BigDecimal): Boolean {
    return when (this) {
      is Int -> BigDecimal(this) <= value
      is Long -> BigDecimal(this) <= value
      is Double -> BigDecimal(this) <= value
      is BigDecimal -> this <= value
      is Number -> BigDecimal(this.toString()) <= value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with BigDecimal (${value}) using <=")
    }
  }

  infix fun Any.eq(value: Int): Boolean {
    return when (this) {
      is Int -> this == value
      is Long -> this == value.toLong()
      is Double -> this == value.toDouble()
      is BigDecimal -> this == BigDecimal(value)
      is Number -> this.toDouble() == value.toDouble()
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Int (${value}) using ==")
    }
  }

  infix fun Any.eq(value: Long): Boolean {
    return when (this) {
      is Int -> this.toLong() == value
      is Long -> this == value
      is Double -> this == value.toDouble()
      is BigDecimal -> this == BigDecimal(value)
      is Number -> this.toLong() == value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Long (${value}) using ==")
    }
  }

  infix fun Any.eq(value: Double): Boolean {
    return when (this) {
      is Int -> this.toDouble() == value
      is Long -> this.toDouble() == value
      is Double -> this == value
      is BigDecimal -> this.toDouble() == value
      is Number -> this.toDouble() == value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Double (${value}) using ==")
    }
  }

  infix fun Any.eq(value: String): Boolean {
    return when (this) {
      is String -> this.equals(value, ignoreCase = true)
      else -> this.toString().equals(value, ignoreCase = true)
    }
  }

  infix fun Any.eq(value: Boolean): Boolean {
    return when (this) {
      is Boolean -> this == value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Boolean (${value}) using ==")
    }
  }

  infix fun Any.ne(value: Int): Boolean {
    return !when (this) {
      is Int -> this == value
      is Long -> this == value.toLong()
      is Double -> this == value.toDouble()
      is BigDecimal -> this == BigDecimal(value)
      is Number -> this.toDouble() == value.toDouble()
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Int (${value}) using !=")
    }
  }

  infix fun Any.ne(value: Long): Boolean {
    return !when (this) {
      is Int -> this.toLong() == value
      is Long -> this == value
      is Double -> this == value.toDouble()
      is BigDecimal -> this == BigDecimal(value)
      is Number -> this.toLong() == value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Long (${value}) using !=")
    }
  }

  infix fun Any.ne(value: Double): Boolean {
    return !when (this) {
      is Int -> this.toDouble() == value
      is Long -> this.toDouble() == value
      is Double -> this == value
      is BigDecimal -> this.toDouble() == value
      is Number -> this.toDouble() == value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Double (${value}) using !=")
    }
  }

  infix fun Any.ne(value: String): Boolean {
    return !when (this) {
      is String -> this.equals(value, ignoreCase = true)
      else -> this.toString().equals(value, ignoreCase = true)
    }
  }

  infix fun Any.ne(value: Boolean): Boolean {
    return !when (this) {
      is Boolean -> this == value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Boolean (${value}) using !=")
    }
  }

  fun param(id: String): Any {
    return metadata[id]?.data ?: throw IllegalStateException("Metadata with id $id does not exist")
  }

  operator fun Any.contains(item: Any): Boolean {
    return when (this) {
      is Collection<*> -> {
        this.any { it == item }
      }

      is Array<*> -> {
        this.any { it == item }
      }

      is String -> {
        this.contains(item.toString())
      }

      is Map<*, *> -> {
        if (item is Pair<*, *>) {
          this.entries.any { it.key == item.first && it.value == item.second }
        } else {
          this.keys.any { it == item } || this.values.any { it == item }
        }
      }

      is IntRange -> {
        item is Int && this.contains(item)
      }

      is LongRange -> {
        item is Long && this.contains(item)
      }

      is CharRange -> {
        item is Char && this.contains(item)
      }

      else -> false
    }
  }

  operator fun <T> get(vararg elements: T): List<T> {
    return elements.toList()
  }

  infix fun Any.hasKey(key: Any): Boolean {
    return when (this) {
      is Map<*, *> -> this.containsKey(key)
      else -> false
    }
  }

  infix fun Any.hasValue(value: Any): Boolean {
    return when (this) {
      is Map<*, *> -> this.containsValue(value)
      is Collection<*> -> this.contains(value)
      is Array<*> -> this.contains(value)
      is String -> this.contains(value.toString())
      else -> false
    }
  }

  infix fun Any.has(value: Any): Boolean {
    return when (this) {
      is Map<*, *> -> this.containsValue(value)
      is Collection<*> -> this.contains(value)
      is Array<*> -> this.contains(value)
      is String -> this.contains(value.toString())
      else -> false
    }
  }

  fun Any.keys(): Collection<*> {
    return when (this) {
      is Map<*, *> -> this.keys
      else -> throw IllegalStateException("Cannot get keys from ${this::class.simpleName} (${this})")
    }
  }

  fun Any.values(): Collection<*> {
    return when (this) {
      is Map<*, *> -> this.values
      else -> throw IllegalStateException("Cannot get values from ${this::class.simpleName} (${this})")
    }
  }

  fun Any.size(): Int {
    return when (this) {
      is Map<*, *> -> this.size
      is Collection<*> -> this.size
      is Array<*> -> this.size
      is String -> this.length
      else -> throw IllegalStateException("Cannot get size of ${this::class.simpleName} (${this})")
    }
  }

  fun Any.isEmpty(): Boolean {
    return when (this) {
      is Map<*, *> -> this.isEmpty()
      is Collection<*> -> this.isEmpty()
      is Array<*> -> this.isEmpty()
      is String -> this.isEmpty()
      else -> true
    }
  }

  fun Any.path(pathString: String): Any {
    val parts = pathString.split('.')
    var current: Any? = this

    for (part in parts) {
      current = when (current) {
        is Map<*, *> -> current[part]
        else -> null
      }

      if (current == null) break
    }

    if (current == null) throw IllegalStateException("Path $pathString not found")

    return current
  }

  infix fun Any.and(value: Boolean): Boolean {
    return when (this) {
      is Boolean -> this && value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Boolean (${value}) using &&")
    }
  }

  infix fun Any.and(value: Any): Boolean {
    return when (this) {
      is Boolean -> when (value) {
        is Boolean -> this && value
        else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Boolean (${value}) using &&")
      }
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Boolean (${value}) using &&")
    }
  }

  infix fun Any.or(value: Boolean): Boolean {
    return when (this) {
      is Boolean -> this || value
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Boolean (${value}) using ||")
    }
  }

  infix fun Any.or(value: Any): Boolean {
    return when (this) {
      is Boolean -> when (value) {
        is Boolean -> this || value
        else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Boolean (${value}) using ||")
      }
      else -> throw IllegalStateException("Cannot compare ${this::class.simpleName} (${this}) with Boolean (${value}) using ||")
    }
  }

}
