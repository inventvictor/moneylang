package moneylang.stages

import moneylang.utils.AppliedTag
import moneylang.utils.Metadata
import moneylang.dsl.MoneyLangDsl
import java.math.BigDecimal

@MoneyLangDsl
class ConditionStageSetupBuilder(
  private val appliedTags: MutableMap<String, AppliedTag>,
  private val metadata: MutableMap<String, Metadata>,
) {

  fun applyTag(tag: String, condition: Boolean) {
    appliedTags[tag] = AppliedTag(tag, condition)
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
