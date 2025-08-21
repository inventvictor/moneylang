package moneylang.stages

import moneylang.utils.Account
import moneylang.utils.CurrencyAmount
import moneylang.utils.Metadata
import moneylang.dsl.MoneyLangDsl
import moneylang.utils.Variable
import java.math.BigDecimal

@MoneyLangDsl
class GivenStageSetupBuilder(
  private val accounts: MutableMap<String, Account> = mutableMapOf(),
  private val metadata: MutableMap<String, Metadata> = mutableMapOf(),
  private val variables: MutableMap<String, Variable> = mutableMapOf()
) {
  fun account(id: String, balance: CurrencyAmount, allowOverdraft: Boolean = false, overdraftLimit: Number = 0.0) {
    accounts[id] = Account(id, BigDecimal.valueOf(balance.getValueAsDouble()), BigDecimal.valueOf(balance.getValueAsDouble()), balance.currency, allowOverdraft, BigDecimal.valueOf(overdraftLimit.toDouble()))
  }

  fun metadata(id: String, data: Any) {
    metadata[id] = Metadata(id, data)
  }

  fun variable(placeholder: String, data: Any) {
    if (!placeholder.startsWith("%%") || !placeholder.endsWith("%%")) {
      throw IllegalArgumentException("Invalid placeholder: $placeholder. Must start and end with '%%'")
    }
    variables[placeholder] = Variable(placeholder, data)
  }
}
