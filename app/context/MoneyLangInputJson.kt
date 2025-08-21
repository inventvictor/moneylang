package moneylang.context

import moneylang.utils.Account
import moneylang.utils.Metadata
import moneylang.utils.Variable

data class MoneyLangInputJson(
  val accounts: List<Account>,
  val metadata: List<Metadata>,
  val variables: List<Variable>
) {
  init {
    val invalidPlaceholder = variables
      .firstOrNull {
        !it.placeholder.startsWith("%%") || !it.placeholder.endsWith("%%")
      }

    if (invalidPlaceholder != null) {
      throw IllegalArgumentException(
        "Invalid placeholder: $invalidPlaceholder. Must start and end with '%%'"
      )
    }
  }
}
