package moneylang.processor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import moneylang.context.MoneyLangContext
import moneylang.context.MoneyLangInputJson
import moneylang.utils.AppliedTag
import moneylang.results.TransactionResults
import moneylang.stages.ConditionStageSetupBuilder
import moneylang.stages.GivenStageSetupBuilder
import moneylang.stages.SendStageBuilder
import moneylang.utils.Account
import moneylang.utils.Metadata
import moneylang.utils.Variable
import java.math.BigDecimal

class TransactionFlow {
  private var accounts = mutableMapOf<String, Account>()
  private var metadata = mutableMapOf<String, Metadata>()
  private var variables = mutableMapOf<String, Variable>()
  private val appliedTags = mutableMapOf<String, AppliedTag>()
  private var transaction: Transaction? = null
  private var postingResults: Map<String, Posting>? = null
  private var transactionResults: TransactionResults? = null

  enum class State {
    INITIAL, GIVEN, SEND, CONDITION
  }

  var currentState: State = State.INITIAL

  fun given(block: GivenStageSetupBuilder.() -> Unit): TransactionFlow {
    if (currentState != State.INITIAL) {
      throw IllegalStateException("The 'given' stage must be the first stage of the transaction flow.")
    }

    val builder = GivenStageSetupBuilder(accounts, metadata, variables)
    builder.block()

    currentState = State.GIVEN
    return this

  }

  fun send(block: SendStageBuilder.() -> Unit): TransactionFlow {
    if (currentState != State.GIVEN) {
      val inputJson = MoneyLangContext.inputJson
        ?: throw IllegalStateException("No input JSON provided. The 'send' stage must be called after the 'given' stage")
      val mapper = jacksonObjectMapper()
      val inputJsonObject : MoneyLangInputJson = mapper.readValue(inputJson)

      if (inputJsonObject.accounts.isEmpty()) {
        throw IllegalStateException("No accounts provided.")
      }

      accounts = inputJsonObject.accounts.associateBy { it.id }.toMutableMap()
      metadata = inputJsonObject.metadata.associateBy { it.id }.toMutableMap()
      variables = inputJsonObject.variables.associateBy { it.placeholder }.toMutableMap()
    }

    val builder = SendStageBuilder(variables, accounts, metadata)
    builder.block()
    transaction = builder.build()

    currentState = State.SEND
    return this

  }

  fun condition(block: ConditionStageSetupBuilder.() -> Unit): TransactionResults {
    if (currentState != State.SEND) {
      throw IllegalStateException("The 'condition' stage must be the last stage and called after 'send'.")
    }

    val builder = ConditionStageSetupBuilder(appliedTags, metadata)
    builder.block()

    val processor = TransactionProcessor()
    postingResults = processor.process(transaction!!, appliedTags)

    applyTransaction(postingResults!!)

    transactionResults = TransactionResults(
      transaction = transaction,
      postings = postingResults ?: emptyMap(),
      accounts = accounts.toMap(),
    )

    getResults().printPostings()
    println()
    getResults().printBalances()

    currentState = State.CONDITION
    return getResults()
  }

  fun getResults(): TransactionResults {
    return transactionResults ?: throw IllegalStateException("No transaction results available. Make sure condition stage was executed.")
  }

  private fun applyTransaction(postings: Map<String, Posting>) {
    postings.values.forEach { posting ->
      val sourceAccount = accounts.getOrPut(posting.source.id) {
        Account(posting.source.id, BigDecimal.ZERO, BigDecimal.ZERO, posting.amount.currency)
      }
      sourceAccount.balance = sourceAccount.balance.subtract(BigDecimal.valueOf(posting.amount.value.toDouble()))

      if (sourceAccount.balance.signum() < 0 && sourceAccount.balance.abs() > sourceAccount.overdraftLimit) {
        throw IllegalArgumentException(
          "Account '${sourceAccount.id}' is overdrawn by '${sourceAccount.balance.abs()}' " +
            "which exceeds the allowed overdraft limit of '${sourceAccount.overdraftLimit}'."
        )
      }

      val destAccount = accounts.getOrPut(posting.destination.id) {
        Account(posting.destination.id, BigDecimal.ZERO, BigDecimal.ZERO, posting.amount.currency)
      }
      destAccount.balance = destAccount.balance.add(BigDecimal.valueOf(posting.amount.value.toDouble()))
    }
  }
}
