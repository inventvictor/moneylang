package moneylang.integration

import moneylang.dsl.moneylang
import moneylang.utils.*
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MoneyLangIntegrationTest {

  @Test
  fun testEcommerceOrderProcessing() {
    val result = moneylang {
      given {
        account("@customer:buyer", NGN(12000))
        account("@platform:fees", NGN(0))
        account("@payment:processor", NGN(0))
        account("@government:vat", NGN(0))
        account("@delivery:service", NGN(0))
        account("@insurance:provider", NGN(0))
        account("@merchant:store", NGN(0))
        
        metadata("order_value", 12000)
        metadata("payment_method", "card")
        metadata("delivery_required", true)
        metadata("insurance_opted", true)
        
        variable("%%delivery_cost%%", 800)
      }
      
      send {
        amount = NGN(12000.00)
        source = "@customer:buyer"
        destination {
          tag("platform_commission") {
            3.percent to "@platform:fees"
          }
          tag("payment_processing") {
            1.5.percent.plus(50) to "@payment:processor"
          }
          tag("tax") {
            7.5.percent to "@government:vat"
          }
          tag("delivery") {
            varNumber("%%delivery_cost%%").exact to "@delivery:service"
          }
          tag("insurance") {
            2.percent.cap(upper = 500, lower = 50) to "@insurance:provider"
          }
          remaining to "@merchant:store"
        }
      }
      
      condition {
        applyTag("platform_commission", condition = param("order_value") gt 1000)
        applyTag("payment_processing", condition = param("payment_method") eq "card")
        applyTag("tax", condition = param("order_value") gt 5000)
        applyTag("delivery", condition = param("delivery_required") eq true)
        applyTag("insurance", condition = param("insurance_opted") eq true)
      }
    }
    
    assertNotNull(result)
    assertTrue(result.contains("postings"))
    assertTrue(result.contains("balances"))
    assertTrue(result.contains("@customer:buyer"))
    assertTrue(result.contains("@merchant:store"))
  }

  @Test
  fun testSubscriptionRevenueSplit() {
    val result = moneylang {
      given {
        account("@subscriber:premium", NGN(2500))
        account("@appstore:commission", NGN(0))
        account("@creator:revenue", NGN(0))
        account("@platform:operations", NGN(0))
        account("@platform:development", NGN(0))
        account("@platform:support", NGN(0))
        
        metadata("payment_channel", "app_store")
        metadata("creator_active", true)
        metadata("subscription_tier", "premium")
      }
      
      send {
        amount = NGN(2500.00)
        source = "@subscriber:premium"
        destination {
          tag("app_store_fee") {
            30.percent to "@appstore:commission"
          }
          tag("content_creator") {
            60.percent to "@creator:revenue"
          }
          tag("platform_operations") {
            remaining to {
              70.percent to "@platform:operations"
              20.percent to "@platform:development"
              10.percent to "@platform:support"
            }
          }
        }
      }
      
      condition {
        applyTag("app_store_fee", condition = param("payment_channel") eq "app_store")
        applyTag("content_creator", condition = param("creator_active") eq true)
        applyTag("platform_operations", condition = param("subscription_tier") eq "premium")
      }
    }
    
    assertNotNull(result)
    assertTrue(result.contains("@subscriber:premium"))
    assertTrue(result.contains("@creator:revenue"))
  }

  @Test
  fun testOverdraftTransaction() {
    val result = moneylang {
      given {
        account(
          "@business:operating",
          balance = NGN(5000),
          allowOverdraft = true,
          overdraftLimit = 20000
        )
        account("@bank:overdraft_fees", NGN(0))
        account("@bank:interest", NGN(0))
        account("@supplier:payment", NGN(0))
        
        metadata("account_balance", -10000)  // Indicates overdraft usage
        metadata("overdraft_used", 10000)
      }
      
      send {
        amount = NGN(15000.00)  // More than balance, uses overdraft
        source = "@business:operating"
        destination {
          tag("overdraft_fee") {
            2.5.percent to "@bank:overdraft_fees"
          }
          tag("interest_charge") {
            1.5.percent to "@bank:interest"
          }
          remaining to "@supplier:payment"
        }
      }
      
      condition {
        applyTag("overdraft_fee", condition = param("account_balance") lt 0)
        applyTag("interest_charge", condition = param("overdraft_used") gt 0)
      }
    }
    
    assertNotNull(result)
    assertTrue(result.contains("@business:operating"))
    assertTrue(result.contains("@supplier:payment"))
  }

  @Test
  fun testMultiCurrencyTransaction() {
    val result = moneylang {
      given {
        account("@customer:international", USD(1000))
        account("@platform:fees", USD(0))
        account("@fx:conversion_fees", USD(0))
        account("@merchant:global", USD(0))
        
        metadata("currency", "USD")
      }
      
      send {
        amount = USD(1000.00)
        source = "@customer:international"
        destination {
          tag("platform_fees") {
            5.percent to "@platform:fees"
          }
          tag("fx_fees") {
            2.5.percent to "@fx:conversion_fees"
          }
          remaining to "@merchant:global"
        }
      }
      
      condition {
        applyTag("platform_fees", condition = param("currency") eq "USD")
        applyTag("fx_fees", condition = param("currency") ne "NGN")
      }
    }
    
    assertNotNull(result)
    assertTrue(result.contains("USD"))
  }

  @Test
  fun testConditionalCashbackSystem() {
    val result = moneylang {
      given {
        account("@customer:gold", NGN(5000))
        account("@merchant", NGN(0))
        account("@customer:cashback", NGN(0))
        
        metadata("loyalty_tier", "gold")
        metadata("cashback_eligible", true)
        
        variable("%%cashback_rate%%", 2.0)
        variable("%%max_cashback%%", 100)
      }
      
      send {
        amount = NGN(5000.00)
        source = "@customer:gold"
        destination {
          tag("cashback") {
            varNumber("%%cashback_rate%%").percent.cap(
              upper = varNumber("%%max_cashback%%"),
              lower = 0
            ) to "@customer:cashback"
          }
          remaining to "@merchant"
        }
      }
      
      condition {
        applyTag("cashback", 
          condition = (param("loyalty_tier") in listOf("gold", "platinum")) and
                     (param("cashback_eligible") eq true)
        )
      }
    }
    
    assertNotNull(result)
    assertTrue(result.contains("@customer:cashback"))
  }

  @Test
  fun testComplexMarketplaceWithMultipleConditions() {
    val result = moneylang {
      given {
        account("@buyer", NGN(10000))
        account("@seller", NGN(0))
        account("@platform:basic_fee", NGN(0))
        account("@platform:premium_fee", NGN(0))
        account("@delivery:standard", NGN(0))
        account("@delivery:express", NGN(0))
        account("@insurance", NGN(0))
        
        metadata("user_tier", "premium")
        metadata("order_amount", 10000)
        metadata("delivery_type", "express")
        metadata("insurance_required", true)
        metadata("country", "Nigeria")
      }
      
      send {
        amount = NGN(10000.00)
        source = "@buyer"
        destination {
          tag("basic_platform_fee") {
            2.percent to "@platform:basic_fee"
          }
          tag("premium_platform_fee") {
            1.5.percent to "@platform:premium_fee"
          }
          tag("standard_delivery") {
            300.exact to "@delivery:standard"
          }
          tag("express_delivery") {
            800.exact to "@delivery:express"
          }
          tag("insurance") {
            1.percent.cap(upper = 200, lower = 50) to "@insurance"
          }
          remaining to "@seller"
        }
      }
      
      condition {
        applyTag("basic_platform_fee", condition = param("user_tier") eq "basic")
        applyTag("premium_platform_fee", condition = param("user_tier") eq "premium")
        applyTag("standard_delivery", condition = param("delivery_type") eq "standard")
        applyTag("express_delivery", condition = param("delivery_type") eq "express")
        applyTag("insurance", 
          condition = (param("insurance_required") eq true) and 
                     (param("order_amount") gt 5000)
        )
      }
    }
    
    assertNotNull(result)
    assertTrue(result.contains("@seller"))
    assertTrue(result.contains("premium"))
  }
}
