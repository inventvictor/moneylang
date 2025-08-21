package moneylang

import moneylang.dsl.moneylang
import moneylang.utils.*

class MoneyLang {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {

      moneylang {
        given {
          account("@orders:1234", NGN(1000))
          account("@platform:commission:sales_tax", NGN(0))
          account("@platform:commission:revenue", NGN(0))
          account("@users:1234:cashback", NGN(0))
          account("@merchants:6789", NGN(0))

          metadata("kyc_level", 2)
          metadata("apply_tax", false)
          metadata("customer_tier", "gold")
          metadata("is_cash_back_eligible", true)
          metadata("status", "ACTIVE")
          metadata("state", listOf("ACTIVE", "CLOSED"))
          metadata("products", listOf("A", "B", "C"))
          metadata("categories", listOf("Electronics", "Food", "Groceries"))
          metadata("rank", 10)
          metadata("membership",
            mapOf(
              "id" to 1234, "name" to "Premium",
              "address" to mapOf(
                "city" to "San Francisco",
                "country" to "USA",
              ),
            ),
          )

          variable("%%merchant_account%%", "@victor")
          variable("%%sales_percentage%%", 3.4)
          variable("%%merchant_amount%%", 2500)
          variable("%%platform_fee%%", 500)
          variable("%%creator_account%%", "@creator")
        }

        send {
          amount = NGN(1000.00)
          source = "@orders:1234" // overdraft upto(100)
          destination {

            tag("victor") {
              5.percent to varString("%%merchant_account%%")
            }

            tag("creator") {
              1.percent.plus(varNumber("%%platform_fee%%")) to varString("%%creator_account%%")
            }

            tag("sales") {
              varNumber("%%sales_percentage%%").percent to {
                tag("sales_tax") {
                  20.percent to "@platform:commission:sales_tax"
                }

                tag("revenue") {
                  remaining to "@platform:commission:revenue"
                }
              }
            }

            tag("ffg") {
              5.percent to "@ffg"
            }

//            tag("rewards") {
//              1.5.exact.plus(100.00, condition = amount lt varNumber("%%merchant_amount%%")) to {
//                tag("cashback") {
//                  upto(110.00) to "@users:1234:cashback"
//                }
//
//                tag("merchant") {
//                  remaining to "@temi"
//                }
//              }
//            }

            tag("liverpool") {
              5.percent to "@liverpool"
            }

            tag("source") {
              remaining to {
                5.exact to "@testing"
                10.exact to "@temi"
                upto(100.00) to "@taiwo"
                upto(100.00) to "@kehinde"
                atleast(100.00) to "@idowu"
              }
            }

          }
        }

        condition {
          applyTag("victor", condition = param("kyc_levelr") gte 1)
          applyTag("sales_tax", condition = param("apply_tax") eq true)
          applyTag("cashback", condition = param("is_cash_back_eligible") eq true and (param("customer_tier") eq "gold") or (param("kyc_level") eq 3 ))
          applyTag("merchant", condition = param("rank") in 1..100)
          applyTag("has_product_a", condition = param("products") has "A")
          applyTag("has_electronics", condition = param("categories") has "Electronics")
          applyTag("merchant", condition = param("membership").path("address").path("country") eq "USA")
        }
      }

      moneylang {
        given {
          account("@victor", balance = NGN(0.00))
          account("@ayo", balance = NGN(1000.00))
          account("@temi", balance = NGN(0.00))

          metadata("should_i_spend", false)
        }

        send {
          amount = NGN(1000.00)
          source = "@ayo"
          destination {
            tag("spending") {
              upto(500) to "@victor"
            }
            remaining to "@temi"
          }
        }

        condition {
          applyTag("spending", condition = param("should_i_spend") eq true)
        }
      }

      moneylang {
        given {
          account(
            "@customer_123",
            balance = NGN(100000.00),
            allowOverdraft = true,
            overdraftLimit = 4500,
          )
          account("@etrack", balance = NGN(0.00))
          account("@estate", balance = NGN(0.00))
          account("@mojec", balance = NGN(0.00))
          account("@paystack", balance = NGN(0.00))
          account("@flutterwave", balance = NGN(0.00))

          metadata("payment_processor", "paystack")

          variable("%%estate_remittance%%", 95.5)
        }

        send {
          amount = NGN(103000.00)
          source = "@customer_123"
          destination {
            tag("paystack") {
              1.5.percent.plus(100, condition = amount gt 2500) to "@paystack"
            }

            tag("flutterwave") {
              2.percent to "@flutterwave"
            }

            tag("estate_remittance") {
              varNumber("%%estate_remittance%%").percent to "@estate"
            }

            tag("etrack_fees") {
              0.34.percent.cap(upper = 500, lower = 50) to "@etrack"
            }

            tag("mojec_fees") {
              remaining to "@mojec"
            }

          }
        }

        condition {
          applyTag("paystack", condition = param("payment_processor") eq "paystack")
          applyTag("flutterwave", condition = param("payment_processor") eq "flutterwave")
        }
      }
    }
  }
}
