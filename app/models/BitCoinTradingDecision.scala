package models

object BitCoinTradingDecision extends Enumeration {
  val Buy = Value("buy")
  val Sell = Value("sell")
  val Hold = Value("hold")
  type BitCoinTradingDecision = Value
}
