package services

import models.{BitCoinInstantPrice, BitCoinTradingDecision, BitCoinTradingDecisionStrategy}
import org.joda.time.Instant
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import services.impl.StrategicalBitCoinTradingDecisionService
import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
 

class StrategicalBitCoinTradingDecisionServiceSpec extends WordSpec with MockFactory with Matchers {

  private val bitCoinPriceForecastingServiceMock = mock[BitCoinPriceForecastingService]
  private val coinBaseApiServiceMock = mock[CoinBaseApiService]
  private val TestAwaitDuration = 1 minute
  private val TestForecastedBitCoinPricesForBuyUnderSafeStrategy = Seq.fill(15)(1600.6)
  private val TestForecastedBitCoinPricesForBuyUnderSafeStrategyFuture = Future(TestForecastedBitCoinPricesForBuyUnderSafeStrategy)
  private val TestForecastedBitCoinPricesForBuyUnderOptimisticStrategy = Seq.fill(15)(1491.0)
  private val TestForecastedBitCoinPricesForBuyUnderOptimisticStrategyFuture = Future(TestForecastedBitCoinPricesForBuyUnderOptimisticStrategy)
  private val TestForecastedBitCoinPricesForHoldUnderSafeStrategy = Seq.fill(15)(1520.0)
  private val TestForecastedBitCoinPricesForHoldUnderSafeStrategyFuture = Future(TestForecastedBitCoinPricesForHoldUnderSafeStrategy)
  private val TestForecastedBitCoinPricesForSellUnderSafeStrategy = Seq.fill(15)(1518.0)
  private val TestForecastedBitCoinPricesForSellUnderSafeStrategyFuture = Future(TestForecastedBitCoinPricesForSellUnderSafeStrategy)
  
  
  
  private val TestInstant = Instant.parse("2018-01-01")
  private val TestBitCoinInstantPrice = BitCoinInstantPrice(time = TestInstant, price = "1490.0")
  private val TestBitCoinInstantPriceFuture = Future(TestBitCoinInstantPrice)

  "getDecision" should {
    "return a buy" when {
      "forecasted price is above 5% than current price under Safe strategy" in {
        (bitCoinPriceForecastingServiceMock.forecast _).expects(*).returning(
          TestForecastedBitCoinPricesForBuyUnderSafeStrategyFuture) once()
        (coinBaseApiServiceMock.getHistoricalPricesByDate _).expects(*).returning(TestBitCoinInstantPriceFuture) once()

        val strategicalBitCoinTradingDecisionService =
          new StrategicalBitCoinTradingDecisionService(bitCoinPriceForecastingServiceMock, coinBaseApiServiceMock)

        val decistionFuture = strategicalBitCoinTradingDecisionService.getDecision()
        val decision = Await.result(decistionFuture, TestAwaitDuration)

        decision should be(BitCoinTradingDecision.Buy)
      }

      "forecasted price is more than current price under optimistic strategy" in {
        (bitCoinPriceForecastingServiceMock.forecast _).expects(*).returning(
          TestForecastedBitCoinPricesForBuyUnderOptimisticStrategyFuture) once()
        (coinBaseApiServiceMock.getHistoricalPricesByDate _).expects(*).returning(TestBitCoinInstantPriceFuture) once()

        val strategicalBitCoinTradingDecisionService =
          new StrategicalBitCoinTradingDecisionService(bitCoinPriceForecastingServiceMock, coinBaseApiServiceMock)

        val decistionFuture = strategicalBitCoinTradingDecisionService.getDecision(decisionStrategy = BitCoinTradingDecisionStrategy.Optimistic)
        val decision = Await.result(decistionFuture, TestAwaitDuration)

        decision should be(BitCoinTradingDecision.Buy)
      }
    }

    "return a hold" when {
      "forecasted price is above 2% than current price under Safe strategy" in {
        (bitCoinPriceForecastingServiceMock.forecast _).expects(*).returning(
          TestForecastedBitCoinPricesForHoldUnderSafeStrategyFuture) once()
        (coinBaseApiServiceMock.getHistoricalPricesByDate _).expects(*).returning(TestBitCoinInstantPriceFuture) once()

        val strategicalBitCoinTradingDecisionService =
          new StrategicalBitCoinTradingDecisionService(bitCoinPriceForecastingServiceMock, coinBaseApiServiceMock)

        val decistionFuture = strategicalBitCoinTradingDecisionService.getDecision()
        val decision = Await.result(decistionFuture, TestAwaitDuration)

        decision should be(BitCoinTradingDecision.Hold)
      }
    }

    "return a sell" when {
      "forecasted price is below 2% than current price under Safe strategy" in {
        (bitCoinPriceForecastingServiceMock.forecast _).expects(*).returning(
          TestForecastedBitCoinPricesForSellUnderSafeStrategyFuture) once()
        (coinBaseApiServiceMock.getHistoricalPricesByDate _).expects(*).returning(TestBitCoinInstantPriceFuture) once()

        val strategicalBitCoinTradingDecisionService =
          new StrategicalBitCoinTradingDecisionService(bitCoinPriceForecastingServiceMock, coinBaseApiServiceMock)

        val decistionFuture = strategicalBitCoinTradingDecisionService.getDecision()
        val decision = Await.result(decistionFuture, TestAwaitDuration)

        decision should be(BitCoinTradingDecision.Sell)
      }
    }
  }
}