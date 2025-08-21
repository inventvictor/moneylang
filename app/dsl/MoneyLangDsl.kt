package moneylang.dsl

import moneylang.processor.TransactionFlow

@DslMarker
annotation class MoneyLangDsl

fun moneylang(block: TransactionFlow.() -> Unit): String {
  val flow = TransactionFlow()
  flow.block()

  if (flow.currentState != TransactionFlow.State.CONDITION) {
    throw IllegalStateException("The 'condition' stage must be the last stage")
  }

  return flow.getResults().toJsonString()
}
