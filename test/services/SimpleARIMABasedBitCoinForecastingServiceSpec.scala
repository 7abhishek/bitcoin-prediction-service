package services

import models.BitCoinInstantPrice
import org.joda.time.{DurationFieldType, Instant}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import services.impl.SimpleARIMABasedBitCoinForecastingService
import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class SimpleARIMABasedBitCoinForecastingServiceSpec extends WordSpec with MockFactory with Matchers with ScalaFutures {

  private val coinBaseApiServiceMock = mock[CoinBaseApiService]
  private val ValidForecastDays = 10
  private val TestInstantStart = Instant.parse("2017-08-16")
  private val TestBitCoinPriceInUSD = 1490
  private val HistoricalBitCoinPricesForYearFuture = Future(getHistoricalBitCoinPricesForYear())
  private val HistoricalInvalidBitCoinPricesForYearFuture = Future(getHistoricalBitCoinPricesForYear(valid= false))
  //Tech Debt: use Await.result until this issue of prematurely timingout of scalaFutures is fixed.
  private implicit val defaultPatience = PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))
  
  "forecast" should {
    "return forecasted bitCoin prices" when {
      "the data could be fit into ARIMA model" in{
        (coinBaseApiServiceMock.getHistoricalPricesByDuration _).expects(*).returning(HistoricalBitCoinPricesForYearFuture)
        
        val bitCoinPriceForecastingService = new SimpleARIMABasedBitCoinForecastingService(coinBaseApiServiceMock)
        val forecastFuture = bitCoinPriceForecastingService.forecast(ValidForecastDays)
        
        whenReady(forecastFuture){ forecastedPrices =>
          forecastedPrices.size should be(ValidForecastDays)
        }
      }
    }

    "throw an exception" when {
      "the coinBaseApiService throws an exception" in {
        (coinBaseApiServiceMock.getHistoricalPricesByDuration _).expects(*).returning(Future(throw new Exception))

        val bitCoinPriceForecastingService = new SimpleARIMABasedBitCoinForecastingService(coinBaseApiServiceMock)
        val forecastFuture = bitCoinPriceForecastingService.forecast(ValidForecastDays)

        an [Exception] should be thrownBy(whenReady(forecastFuture)(forecast => forecast))
      }

      "the data could not be fit into ARIMA model" in {
        (coinBaseApiServiceMock.getHistoricalPricesByDuration _).expects(*).returning(HistoricalInvalidBitCoinPricesForYearFuture)

        val bitCoinPriceForecastingService = new SimpleARIMABasedBitCoinForecastingService(coinBaseApiServiceMock)
        val forecastFuture = bitCoinPriceForecastingService.forecast(ValidForecastDays)

        an [Exception] should be thrownBy(whenReady(forecastFuture)(forecast => forecast))
      }
    }
  }
  
  private def getHistoricalBitCoinPricesForYear(valid: Boolean = true): Seq[BitCoinInstantPrice] = {
    valid match {
      case true =>
        val random = new Random
        (0 to 364).map(dayCount => {
          val currentdayInstant = TestInstantStart.toDateTime.withFieldAdded(DurationFieldType.days(), dayCount).toInstant
          val testPredictedPrice = 1 + random.nextInt(TestBitCoinPriceInUSD + dayCount)
          BitCoinInstantPrice(time = currentdayInstant, price = testPredictedPrice.toString)
        })
      case _ =>
        (0 to 364).map(dayCount => {
          val currentdayInstant = TestInstantStart.toDateTime.withFieldAdded(DurationFieldType.days(), dayCount).toInstant
          val testPredictedPrice = TestBitCoinPriceInUSD + dayCount
          BitCoinInstantPrice(time = currentdayInstant, price = testPredictedPrice.toString)
        })
    }
  }
}
