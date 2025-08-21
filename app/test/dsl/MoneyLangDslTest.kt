package moneylang.dsl

import moneylang.utils.*
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class MoneyLangDslTest {

  @Test
  fun testBasicMoneyLangDslStructure() {
    val result = moneylang {
      given {
        account("@alice", NGN(1000))
        account("@bob", NGN(0))
      }
      
      send {
        amount = NGN(500.00)
        source = "@alice"
        destination {
          remaining to "@bob"
        }
      }
      
      condition {
        // No conditions for simple transfer
      }
    }
    
    assertNotNull(result)
    // Result should be valid JSON string
    assert(result.startsWith("{"))
    assert(result.endsWith("}"))
  }

  @Test
  fun testDslWithPercentageAllocations() {
    val result = moneylang {
      given {
        account("@customer", NGN(1000))
        account("@platform", NGN(0))
        account("@merchant", NGN(0))
        
        metadata("apply_fee", true)
      }
      
      send {
        amount = NGN(1000.00)
        source = "@customer"
        destination {
          tag("platform_fee") {
            5.percent to "@platform"
          }
          remaining to "@merchant"
        }
      }
      
      condition {
        applyTag("platform_fee", condition = param("apply_fee") eq true)
      }
    }
    
    assertNotNull(result)
    assert(result.contains("postings"))
    assert(result.contains("balances"))
  }

  @Test
  fun testDslWithMultipleCurrencies() {
    val result = moneylang {
      given {
        account("@usd_account", USD(1000))
        account("@recipient", USD(0))
      }
      
      send {
        amount = USD(500.00)
        source = "@usd_account"
        destination {
          remaining to "@recipient"
        }
      }
      
      condition {
        // No conditions
      }
    }
    
    assertNotNull(result)
  }

  @Test
  fun testDslWithComplexAllocationTypes() {
    val result = moneylang {
      given {
        account("@source", NGN(10000))
        account("@fee_account", NGN(0))
        account("@bonus_account", NGN(0))
        account("@merchant", NGN(0))
        
        metadata("high_value", true)
        metadata("bonus_eligible", true)
      }
      
      send {
        amount = NGN(10000.00)
        source = "@source"
        destination {
          tag("processing_fee") {
            2.5.percent.cap(upper = 500, lower = 100) to "@fee_account"
          }
          tag("bonus") {
            upto(200) to "@bonus_account"
          }
          remaining to "@merchant"
        }
      }
      
      condition {
        applyTag("processing_fee", condition = param("high_value") eq true)
        applyTag("bonus", condition = param("bonus_eligible") eq true)
      }
    }
    
    assertNotNull(result)
  }

  @Test
  fun testDslThrowsExceptionWhenConditionNotLast() {
    assertFailsWith<IllegalStateException> {
      moneylang {
        given {
          account("@alice", NGN(1000))
          account("@bob", NGN(0))
        }
        
        condition {
          // Condition stage is not last
        }
        
        send {
          amount = NGN(500.00)
          source = "@alice"
          destination {
            remaining to "@bob"
          }
        }
      }
    }
  }

  @Test
  fun testDslWithVariables() {
    val result = moneylang {
      given {
        account("@source", NGN(5000))
        account("@destination", NGN(0))
        
        variable("%%fee_rate%%", 2.5)
        variable("%%target_account%%", "@destination")

        metadata("always_true", true)
      }
      
      send {
        amount = NGN(5000.00)
        source = "@source"
        destination {
          tag("variable_fee") {
            varNumber("%%fee_rate%%").percent to varString("%%target_account%%")
          }
          remaining to "@destination"
        }
      }
      
      condition {
        applyTag("variable_fee", condition = param("always_true") eq true)
      }
    }
    
    assertNotNull(result)
  }

  @Test
  fun testDslWithNestedDestinations() {
    val result = moneylang {
      given {
        account("@source", NGN(2000))
        account("@platform_ops", NGN(0))
        account("@platform_dev", NGN(0))
        account("@merchant", NGN(0))
        
        metadata("apply_split", true)
      }
      
      send {
        amount = NGN(2000.00)
        source = "@source"
        destination {
          tag("platform_share") {
            10.percent to {
              60.percent to "@platform_ops"
              remaining to "@platform_dev"
            }
          }
          remaining to "@merchant"
        }
      }
      
      condition {
        applyTag("platform_share", condition = param("apply_split") eq true)
      }
    }
    
    assertNotNull(result)
  }
}
