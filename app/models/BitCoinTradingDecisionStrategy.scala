package models

object BitCoinTradingDecisionStrategy extends Enumeration {
  val Optimistic = Value("optimistic")
  val Safe = Value("safe")
  type BitCoinTradingDecisionStrategy = Value
}
