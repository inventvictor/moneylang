package moneylang.processor

import moneylang.utils.Allocation

sealed class Destination {
  data class Account(val account: moneylang.utils.Account, val tag: String) : Destination()
  data class Split(val allocations: List<Allocation>, val tag: String) : Destination()
}
