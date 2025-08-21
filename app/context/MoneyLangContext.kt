package moneylang.context

object MoneyLangContext {
  private val threadLocalInputJson = ThreadLocal<String?>()

  var inputJson: String?
    get() = threadLocalInputJson.get()
    set(value) = threadLocalInputJson.set(value)

  fun clear() {
    threadLocalInputJson.remove()
  }
}

