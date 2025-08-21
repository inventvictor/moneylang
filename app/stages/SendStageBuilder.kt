package moneylang.stages

import moneylang.dsl.MoneyLangDsl
import moneylang.processor.Destination
import moneylang.processor.DestinationBuilder
import moneylang.processor.Transaction
import moneylang.utils.Account
import moneylang.utils.CurrencyAmount
import moneylang.utils.Metadata
import moneylang.utils.Variable
import java.math.BigDecimal

@MoneyLangDsl
class SendStageBuilder(private val variables: MutableMap<String, Variable>,
                       private val accounts: MutableMap<String, Account>,
                       private val metadata: MutableMap<String, Metadata>) {
  var amount: CurrencyAmount? = null
  var source: String? = null
  var destination: Destination? = null

  private fun total(): CurrencyAmount {
    return amount ?: throw IllegalStateException("Amount must be specified before referencing it")
  }

  infix fun destination(block: DestinationBuilder.() -> Unit) {
    val builder = DestinationBuilder(BigDecimal.valueOf(total().getValueAsDouble()), variables, accounts, metadata)
    builder.block()
    destination = Destination.Split(builder.allocations, builder.currentTag)
  }

  fun build(): Transaction = Transaction(
    amount = amount ?: throw IllegalStateException("Amount must be specified"),
    source = accounts[source ?: throw IllegalStateException("Source account must be specified")]
      ?: throw IllegalStateException("Account '${source}' not found"),
    destination = destination ?: throw IllegalStateException("Destination must be specified"),
  )
}
