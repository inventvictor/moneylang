# MoneyLang DSL

**A powerful Domain Specific Language for financial transaction processing**

MoneyLang makes complex financial transactions simple and readable. Describe money movements using intuitive, English-like syntax that handles splits, fees, commissions, and conditional logic automatically.

---

## Table of Contents

1. [What is MoneyLang?](#what-is-moneylang)
2. [Quick Start](#quick-start)
3. [Core Concepts](#core-concepts)
4. [Learning the Syntax](#learning-the-syntax)
5. [Practical Examples](#practical-examples)
6. [Advanced Features](#advanced-features)
7. [Best Practices](#best-practices)
8. [Common Patterns](#common-patterns)

---

## What is MoneyLang?

### For Business Users
Instead of complex spreadsheets or manual calculations, describe your financial flows naturally:

- **"Send 70% to the merchant, 2.5% to platform fees, and the rest to delivery"**
- **"Only charge insurance if the customer is premium tier"**
- **"Apply different tax rates based on order value"**
- **"If overdraft is used, charge 2% interest to the bank"**

### For Developers
MoneyLang is a type-safe, declarative DSL that provides:

- **🔢 Precise decimal arithmetic** - No floating-point errors
- **🏷️ Conditional logic** - Tag-based routing with complex conditions
- **💰 Multiple allocation types** - Exact amounts, percentages, caps, limits
- **🏦 Account management** - Overdraft support and balance tracking
- **🔄 Variable substitution** - Dynamic values and configurations
- **📊 Structured output** - JSON results for easy integration

---

## Quick Start

### Your First Transaction

```kotlin
moneylang {
    send {
        amount = NGN(1000.00)
        source = "@alice"
        destination {
            remaining to "@bob"
        }
    }
    condition {
        // No conditions needed for simple transfers
    }
}
```

**What this does**: Transfers 1000 NGN from Alice's account to Bob's account.

### Basic Marketplace Transaction

```kotlin
moneylang {
    send {
        amount = NGN(5000.00)
        source = "@customer"
        destination {
            tag("platform_fee") {
                3.percent to "@platform:fees"
            }
            tag("delivery") {
                500.exact to "@delivery:service"
            }
            remaining to "@merchant"
        }
    }
    condition {
        applyTag("platform_fee", condition = param("order_value") gt 1000)
        applyTag("delivery", condition = param("delivery_required") eq true)
    }
}
```

**What this does**: 
- Takes 3% platform fee (if order > 1000)
- Charges 500 NGN for delivery (if delivery required)
- Sends remainder to merchant

---

## Core Concepts

### 🏦 Accounts
Digital wallets with unique IDs, balances, and optional overdraft limits.

Account structure (configured externally)
```json
{
  "id": "@customer:john",
  "balance": "5000.00",
  "currency": "NGN", 
  "allowOverdraft": true,
  "overdraftLimit": "1000.00"
}
```

### 🏷️ Tags
Conditional labels that control when transactions execute:

```kotlin
tag("premium_bonus") {
    100.exact to "@customer:bonus"
}

// Only executes if condition is met
condition {
    applyTag("premium_bonus", condition = param("tier") eq "premium")
}
```

### 💰 Allocation Types

| Type | Syntax | Description | Example |
|------|--------|-------------|---------|
| **Exact** | `50.exact` | Fixed amount | `100.exact to "@fees"` |
| **Percentage** | `5.percent` | Percentage of total | `2.5.percent to "@commission"` |
| **Remaining** | `remaining` | Whatever is left | `remaining to "@merchant"` |
| **Capped** | `.cap(upper, lower)` | Limited percentage | `5.percent.cap(upper = 500, lower = 50)` |
| **Up to** | `upto(amount)` | Maximum amount | `upto(200) to "@bonus"` |
| **At least** | `atleast(amount)` | Minimum amount | `atleast(100) to "@minimum"` |

### 🔢 Variables
Dynamic values that can be configured externally:

```kotlin
// In DSL
varNumber("%%commission_rate%%").percent to "@platform:commission"
```

External configuration
```json
{
  "placeholder": "%%commission_rate%%",
  "data": 2.5
}
```

### 📊 Metadata
Context information for conditional logic:

```kotlin
// In DSL  
condition {
    applyTag("tax", condition = param("country") eq "Nigeria")
}
```

External configuration 
```json
{
  "id": "country",
  "data": "Nigeria"
}
```

---

## Learning the Syntax

### 1. Basic Structure

Every MoneyLang transaction has two main parts:

```kotlin
moneylang {
    send {
        // Define amount, source, and destinations
    }
    condition {
        // Define when each destination should receive money
    }
}
```

### 2. Simple Destinations

```kotlin
send {
    amount = NGN(1000.00)
    source = "@customer"
    destination {
        // Send all money to merchant
        remaining to "@merchant"
    }
}
```

### 3. Multiple Destinations

```kotlin
destination {
    50.exact to "@processing_fee"         // Fixed fee
    2.5.percent to "@platform_commission" // Percentage
    remaining to "@merchant"              // Everything else
}
```

### 4. Conditional Destinations

```kotlin
destination {
    tag("express_delivery") {
        800.exact to "@delivery:express"
    }
    tag("standard_delivery") {
        300.exact to "@delivery:standard"
    }
    remaining to "@merchant"
}

condition {
    applyTag("express_delivery", condition = param("delivery_type") eq "express")
    applyTag("standard_delivery", condition = param("delivery_type") eq "standard")
}
```

### 5. Nested Allocations

```kotlin
destination {
    tag("platform_share") {
        10.percent to {
            60.percent to "@platform:operations"
            40.percent to "@platform:support"
        }
    }
    remaining to "@merchant"
}
```

---

## Practical Examples

### E-commerce Order Processing

```kotlin
moneylang {
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
```

### Subscription Revenue Split

```kotlin
moneylang {
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
```

### Overdraft Transaction

```kotlin
moneylang {
    send {
        amount = NGN(15000.00)
        source = "@business:operating"  // Balance: 5000, Overdraft: 20000
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
```

---

## Advanced Features

### Conditional Operators

| Operator | Description | Example |
|----------|-------------|---------|
| `eq` | Equals | `param("status") eq "active"` |
| `ne` / `neq` | Not equals | `param("country") ne "Nigeria"` |
| `gt` | Greater than | `param("amount") gt 1000` |
| `gte` | Greater than or equal | `param("orders") gte 5` |
| `lt` | Less than | `param("age") lt 65` |
| `lte` | Less than or equal | `param("score") lte 100` |
| `in` | In range/list | `param("rating") in 1..5` |
| `has` | Collection/Object has value | `param("features") has "premium"` |
| `hasKey` | Object has key | `param("config") hasKey "api_key"` |
| `hasValue` | Object has value | `param("settings") hasValue true` |
| `and` | Logical AND | `condition1 and condition2` |
| `or` | Logical OR | `condition1 or condition2` |
| `..` | Range operator | `param("age") in 18..65` |

### Built-in Functions

| Function | Description | Example |
|----------|-------------|---------|
| `listOf()` | Create a list | `param("tier") in listOf("gold", "platinum")` |
| `mapOf()` | Create a map/object | `param("config") eq mapOf("key" to "value")` |
| `keys()` | Get object keys | `param("data").keys() contains "user_id"` |
| `values()` | Get object values | `param("data").values() contains "premium"` |
| `size()` | Get collection size | `param("items").size() gt 5` |
| `isEmpty()` | Check if empty | `param("cart").isEmpty() eq false` |
| `path()` | Access nested properties | `param("user").path("address.city") eq "Lagos"` |

### Boolean and Numeric Literals

| Type | Syntax | Example |
|------|--------|---------|
| **Boolean** | `true` / `false` | `param("verified") eq true` |
| **Integer** | `123` | `param("count") eq 100` |
| **Decimal** | `12.34` | `param("rate") eq 2.5` |
| **Range** | `1..10` | `param("rating") in 1..5` |

### Complex Conditions

```kotlin
condition {
    // Boolean literals
    applyTag("verified_users", condition = param("verified") eq true)
    applyTag("inactive_users", condition = param("active") eq false)
    
    // Range conditions with range operator
    applyTag("bulk_discount", condition = param("quantity") in 10..100)
    applyTag("adult_content", condition = param("age") in 18..65)
    
    // List creation and membership
    applyTag("premium_countries", condition = param("country") in listOf("US", "UK", "DE", "CA"))
    applyTag("premium_tiers", condition = param("tier") in listOf("gold", "platinum", "diamond"))
    
    // Map/Object operations
    applyTag("has_api_config", condition = param("config") hasKey "api_key")
    applyTag("premium_features", condition = param("features") hasValue "unlimited_storage")
    applyTag("config_complete", condition = param("settings").keys().size() gte 5)
    
    // Collection functions
    applyTag("large_cart", condition = param("cart").size() gt 10)
    applyTag("empty_wishlist", condition = param("wishlist").isEmpty() eq true)
    applyTag("many_orders", condition = param("order_history").values().size() gte 20)
    
    // String and collection contains
    applyTag("premium_feature", condition = param("features") has "premium_support")
    applyTag("vip_tag", condition = param("tags") has "vip")
    
    // Nested object access with path
    applyTag("lagos_customer", condition = param("address").path("city") eq "Lagos")
    applyTag("nigeria_user", condition = param("profile").path("location.country") eq "Nigeria")
    applyTag("verified_email", condition = param("user").path("email.verified") eq true)
    
    // Combined conditions with boolean operators
    applyTag("vip_bonus", 
        condition = param("verified") eq true and 
                   (param("tier") eq "gold" or param("orders").size() gte 10)
    )
    
    // Complex nested conditions
    applyTag("enterprise_discount",
        condition = (param("company").path("size") in listOf("large", "enterprise")) and
                   (param("contract").hasKey("discount_rate")) and
                   (param("payment_history").isEmpty() eq false)
    )
    
    // Numeric comparisons with different operators
    applyTag("high_value", condition = param("order_value") gt 50000)
    applyTag("bulk_order", condition = param("quantity") gte 100)
    applyTag("small_order", condition = param("items").size() lt 5)
    applyTag("exact_match", condition = param("score") eq 100)
    applyTag("not_basic", condition = param("tier") ne "basic")
    applyTag("alternative_not_equal", condition = param("status") neq "pending")
}
```

### Mathematical Operations

```kotlin
destination {
    // Basic arithmetic operations
    5.percent.plus(100) to "@enhanced_fee"           // Add fixed amount to percentage
    10.percent.minus(50) to "@discounted_fee"        // Subtract from percentage  
    3.percent.times(2) to "@doubled_commission"      // Multiply percentage
    8.percent.div(2) to "@half_commission"           // Divide percentage
    
    // Exact amount arithmetic
    100.exact.plus(50) to "@base_plus_extra"         // Add to exact amount
    200.exact.minus(25) to "@discounted_exact"       // Subtract from exact amount
    75.exact.times(2) to "@doubled_exact"            // Multiply exact amount
    150.exact.div(3) to "@split_exact"               // Divide exact amount
    
    // Advanced mathematical combinations
    2.5.percent.plus(100).cap(upper = 500, lower = 150) to "@complex_fee"
    varNumber("%%base_rate%%").percent.times(varNumber("%%multiplier%%")) to "@calculated_rate"
    
    // Conditional mathematical operations
    tag("volume_bonus") {
        5.percent.plus(varNumber("%%bonus_amount%%"), condition = param("volume") gt 10000) to "@volume_bonus"
    }
    
    // Capped operations with mathematical functions
    tag("tiered_fee") {
        varNumber("%%base_fee%%").percent
            .plus(varNumber("%%additional_fee%%"))
            .cap(upper = varNumber("%%max_fee%%"), lower = varNumber("%%min_fee%%")) to "@tiered_commission"
    }
}
```

### BigDecimal Mathematical Functions

```kotlin
condition {
    // Numeric validation functions
    applyTag("positive_balance", condition = param("balance").isPositive() eq true)
    applyTag("zero_balance", condition = param("amount").isZero() eq true)
    applyTag("negative_balance", condition = param("overdraft").isNegative() eq true)
    
    // Range checking
    applyTag("valid_range", condition = param("score").between(0, 100) eq true)
    applyTag("amount_check", condition = param("transaction").between(varNumber("%%min%%"), varNumber("%%max%%")) eq true)
    
    // Min/Max operations
    applyTag("high_amount", condition = param("values").max() gt 1000)
    applyTag("low_amount", condition = param("values").min() lt 100)
    
    // Rounding operations (for display/validation)
    applyTag("rounded_check", condition = param("amount").round(2) eq param("expected"))
    applyTag("rounded_up", condition = param("fee").roundUp() gte param("minimum"))
    applyTag("rounded_down", condition = param("discount").roundDown() lte param("maximum"))
}
```

### Multi-Currency Support

```kotlin
// All supported currencies
send {
    amount = NGN(50000.00)    // Nigerian Naira
    // amount = USD(1000.00)  // US Dollar  
    // amount = EUR(850.00)   // Euro
    // amount = GBP(750.00)   // British Pound
    // amount = CAD(1300.00)  // Canadian Dollar
    // amount = AUD(1400.00)  // Australian Dollar
    // amount = JPY(110000.00) // Japanese Yen
    // amount = CHF(950.00)   // Swiss Franc
    // amount = CNY(6500.00)  // Chinese Yuan
    // amount = INR(75000.00) // Indian Rupee
    
    source = "@customer:international"
    destination {
        5.percent to "@platform:fees"
        2.5.percent to "@fx:conversion_fees"
        remaining to "@merchant:global"
    }
}

condition {
    applyTag("fx_fees", condition = param("currency") ne "NGN")
}
```

### String and Variable Operations

```kotlin
condition {
    // String variable operations
    applyTag("custom_account", condition = varString("%%target_account%%") eq "@merchant:premium")
    applyTag("dynamic_routing", condition = param("routing") eq varString("%%preferred_route%%"))
    
    // Variable number operations  
    applyTag("threshold_met", condition = param("amount") gt varNumber("%%minimum_threshold%%"))
    applyTag("rate_based", condition = varNumber("%%commission_rate%%") gt 2.0)
    
    // Combined variable operations
    applyTag("variable_condition", 
        condition = (param("tier") eq varString("%%target_tier%%")) and 
                   (param("volume") gte varNumber("%%volume_threshold%%"))
    )
}

destination {
    // Variable-based allocations
    tag("dynamic_commission") {
        varNumber("%%commission_rate%%").percent to varString("%%commission_account%%")
    }
    
    tag("calculated_fee") {
        varNumber("%%base_fee%%").exact
            .plus(varNumber("%%additional_fee%%"))
            .times(varNumber("%%multiplier%%")) to "@calculated:fees"
    }
}
```

## Best Practices

### 🎯 Design Guidelines

1. **Start Simple**: Begin with basic transfers, add complexity gradually
2. **Use Descriptive Tags**: `"merchant_commission"` not `"tag1"`
3. **Group Related Logic**: Use nested destinations for sub-allocations
4. **Order Conditions**: Put most likely conditions first for performance
5. **Validate Thoroughly**: Test with various amounts and edge cases

### 💡 Common Patterns

#### Fee Structure
```kotlin
destination {
    tag("processing_fee") {
        2.percent.cap(upper = 200, lower = 25) to "@processor"
    }
    tag("platform_fee") {
        upto(500) to "@platform"
    }
    remaining to "@recipient"
}
```

#### Tiered Allocation
```kotlin
destination {
    tag("revenue_share") {
        30.percent to {
            tag("tier1") {
                50.percent to "@partner:tier1"
            }
            tag("tier2") {
                30.percent to "@partner:tier2"
            }
            remaining to "@partner:general"
        }
    }
    remaining to "@company:revenue"
}
```

#### Conditional Cashback
```kotlin
destination {
    tag("cashback") {
        varNumber("%%cashback_rate%%").percent.cap(
            upper = varNumber("%%max_cashback%%"),
            lower = 0
        ) to "@customer:cashback"
    }
    remaining to "@merchant"
}

condition {
    applyTag("cashback", 
        condition = (param("loyalty_tier") in listOf("gold", "platinum")) and
                (param("cashback_eligible") eq true)
    )
}
```

### 🔒 Security Considerations

1. **Validate Account Access**: Ensure users can only access their accounts
2. **Audit All Transactions**: Log every financial movement
3. **Sanitize Inputs**: Validate all metadata and variables
4. **Use Overdraft Carefully**: Set appropriate limits
5. **Monitor for Abuse**: Watch for unusual patterns

---

## Common Patterns

### Marketplace Transaction
```kotlin
// Platform takes commission, handles fees, pays merchant
3.percent to "@platform:commission"
1.5.percent to "@payment:processing" 
500.exact to "@delivery:service"
remaining to "@merchant"
```

### Subscription Split
```kotlin
// Revenue sharing between platform and content creator
30.percent to "@platform:appstore_fee"
60.percent to "@creator:revenue"
remaining to "@platform:operations"
```

### Financial Services
```kotlin
// Bank transaction with regulatory requirements
0.5.percent to "@central_bank:levy"
1.25.percent to "@bank:interchange"
2.5.percent.cap(upper = 1000) to "@processor:fees"
remaining to "@merchant:settlement"
```

### Overdraft Management
```kotlin
// Handle insufficient funds with fees
tag("overdraft_fee") {
    2.5.percent to "@bank:overdraft_fees"
}
tag("interest") {
    varNumber("%%daily_rate%%").percent to "@bank:interest"
}
remaining to "@recipient"
```

---

## Getting Help

### Debugging Tips
1. **Start with exact amounts** before converting to percentages
2. **Use simple conditions** first, then add complexity
3. **Check tag application** by reviewing condition logic
4. **Validate account balances** before running transactions
5. **Test with small amounts** to verify logic

### Common Mistakes
- Forgetting to apply tags in the condition block
- Using percentages without understanding the base amount
- Not handling the `remaining` allocation
- Mixing currencies in the same transaction
- Setting overdraft limits lower than expected usage

---

MoneyLang makes financial transaction processing intuitive and reliable. Start with simple examples, understand the core concepts, and gradually build more sophisticated financial workflows.

**Ready to start?** Try the examples in this documentation or use the MoneyLang Editor for an interactive experience with syntax highlighting and auto-completion.
