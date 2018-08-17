package models

import org.joda.time.Instant

//TechDebt: use numerical model for price.
case class BitCoinInstantPrice(time: Instant, price: String) {
  require(price.nonEmpty , s"price cannot be empty")
  val numericalPrice = price.toDouble
  require(numericalPrice > 0 , s"numericalPrice greater than 0, but was $numericalPrice")
}