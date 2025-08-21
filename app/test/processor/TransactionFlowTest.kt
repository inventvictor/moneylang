package moneylang.processor

import moneylang.utils.*
import kotlin.test.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class TransactionFlowTest {

  @Test
  fun testTransactionFlowStateProgression() {
    val flow = TransactionFlow()
    
    assertEquals(TransactionFlow.State.INITIAL, flow.currentState)
    
    flow.given {
      account("@test", NGN(1000))
      account("@target", NGN(0))
    }
    assertEquals(TransactionFlow.State.GIVEN, flow.currentState)
    
    flow.send {
      amount = NGN(500.00)
      source = "@test"
      destination {
        remaining to "@target"
      }
    }
    assertEquals(TransactionFlow.State.SEND, flow.currentState)
    
    val results = flow.condition {
      // No conditions
    }
    assertEquals(TransactionFlow.State.CONDITION, flow.currentState)
    assertNotNull(results)
  }

  @Test
  fun testGivenStageNotFirst() {
    val flow = TransactionFlow()
    
    // Artificially change state
    flow.currentState = TransactionFlow.State.SEND
    
    assertFailsWith<IllegalStateException> {
      flow.given {
        account("@test", NGN(1000))
      }
    }
  }

  @Test
  fun testSendStageWithoutGiven() {
    val flow = TransactionFlow()
    
    assertFailsWith<IllegalStateException> {
      flow.send {
        amount = NGN(500.00)
        source = "@test"
        destination {
          remaining to "@target"
        }
      }
    }
  }

  @Test
  fun testConditionStageWithoutSend() {
    val flow = TransactionFlow()
    
    flow.given {
      account("@test", NGN(1000))
    }
    
    assertFailsWith<IllegalStateException> {
      flow.condition {
        // No conditions
      }
    }
  }

  @Test
  fun testSimpleTransfer() {
    val flow = TransactionFlow()
    
    val results = flow
      .given {
        account("@alice", NGN(1000))
        account("@bob", NGN(500))
      }
      .send {
        amount = NGN(300.00)
        source = "@alice"
        destination {
          remaining to "@bob"
        }
      }
      .condition {
        // Simple transfer, no conditions
      }
    
    assertEquals(2, results.accounts.size)
    
    // Alice should have 700 NGN (1000 - 300)
    val aliceAccount = results.accounts["@alice"]
    assertNotNull(aliceAccount)
    assertEquals(BigDecimal("700.00").toDouble(), aliceAccount.balance.toDouble(), 0.0)
    
    // Bob should have 800 NGN (500 + 300)
    val bobAccount = results.accounts["@bob"]
    assertNotNull(bobAccount)
    assertEquals(BigDecimal("800.00").toDouble(), bobAccount.balance.toDouble(), 0.0)
  }

  @Test
  fun testTransferWithPercentage() {
    val flow = TransactionFlow()
    
    val results = flow
      .given {
        account("@customer", NGN(1000))
        account("@platform", NGN(0))
        account("@merchant", NGN(0))
        
        metadata("take_fee", true)
      }
      .send {
        amount = NGN(1000.00)
        source = "@customer"
        destination {
          tag("platform_fee") {
            10.percent to "@platform"
          }
          remaining to "@merchant"
        }
      }
      .condition {
        applyTag("platform_fee", condition = param("take_fee") eq true)
      }
    
    // Customer: 1000 - 1000 = 0
    val customerAccount = results.accounts["@customer"]
    assertNotNull(customerAccount)
    assertEquals(BigDecimal.ZERO.toDouble(), customerAccount.balance.toDouble(), 0.0)
    
    // Platform: 0 + 100 (10% of 1000) = 100
    val platformAccount = results.accounts["@platform"]
    assertNotNull(platformAccount)
    assertEquals(BigDecimal("100.00").toDouble(), platformAccount.balance.toDouble(), 0.0)
    
    // Merchant: 0 + 900 (remaining 90%) = 900
    val merchantAccount = results.accounts["@merchant"]
    assertNotNull(merchantAccount)
    assertEquals(BigDecimal("900.00").toDouble(), merchantAccount.balance.toDouble(), 0.0)
  }

  @Test
  fun testOverdraftTransaction() {
    val flow = TransactionFlow()
    
    val results = flow
      .given {
        account(
          "@customer",
          balance = NGN(100),
          allowOverdraft = true,
          overdraftLimit = 500
        )
        account("@merchant", NGN(0))
      }
      .send {
        amount = NGN(300.00)  // More than balance, but within overdraft
        source = "@customer"
        destination {
          remaining to "@merchant"
        }
      }
      .condition {
        // No conditions
      }
    
    // Customer: 100 - 300 = -200 (within overdraft limit of 500)
    val customerAccount = results.accounts["@customer"]
    assertNotNull(customerAccount)
    assertEquals(BigDecimal("-200.00").toDouble(), customerAccount.balance.toDouble(), 0.0)
    
    // Merchant: 0 + 300 = 300
    val merchantAccount = results.accounts["@merchant"]
    assertNotNull(merchantAccount)
    assertEquals(BigDecimal("300.00").toDouble(), merchantAccount.balance.toDouble(), 0.0)
  }

  @Test
  fun testOverdraftLimitExceeded() {
    val flow = TransactionFlow()
    
    assertFailsWith<IllegalArgumentException> {
      flow
        .given {
          account(
            "@customer",
            balance = NGN(100),
            allowOverdraft = true,
            overdraftLimit = 200  // Limit is 200
          )
          account("@merchant", NGN(0))
        }
        .send {
          amount = NGN(400.00)  // 100 - 400 = -300, exceeds limit
          source = "@customer"
          destination {
            remaining to "@merchant"
          }
        }
        .condition {
          // No conditions
        }
    }
  }

  @Test
  fun testGetResultsBeforeExecution() {
    val flow = TransactionFlow()
    
    assertFailsWith<IllegalStateException> {
      flow.getResults()
    }
  }
}
