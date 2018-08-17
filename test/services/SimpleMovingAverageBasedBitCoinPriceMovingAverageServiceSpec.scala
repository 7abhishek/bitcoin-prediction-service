package services

import models.BitCoinInstantPrice
import org.joda.time.Instant
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import services.impl.SimpleMovingAverageBasedBitCoinPriceMovingAverageService
import scala.collection.immutable.Seq

class SimpleMovingAverageBasedBitCoinPriceMovingAverageServiceSpec extends WordSpec with MockFactory with Matchers {
  private val coinBaseApiServiceMock = mock[CoinBaseApiService]
  private val movingAverageCalculatorServiceMock = mock[MovingAverageCalculatorService]
  private val ValidStartDate = "2018-01-01"
  private val ValidEndDate = "2018-01-15"
  private val DaysBetweenStartNEndDates = 15
  private val MovingAverageNumber = 10
  private val TestInstant = Instant.parse(ValidStartDate)
  private val TestBitCoinInstantPrice = BitCoinInstantPrice(time = TestInstant, price = "1490.0")
  private val TestBitCoinInstantPriceList = Seq.fill(DaysBetweenStartNEndDates)(TestBitCoinInstantPrice)
  private val TestBitCoinPriceList = TestBitCoinInstantPriceList.map(_.numericalPrice)
  private val TestBitCoinInstantPriceListFuture = Future(TestBitCoinInstantPriceList)
  private val TestExpectedMovingAverages = Seq.fill(6)(1490.0)
  private val TestExpectedMovingAveragesFuture = Future(TestExpectedMovingAverages)
  
  
  "average" should {
    "return valid averages" when {
      "data and movingAverageNumber are valid" in {
        (coinBaseApiServiceMock.getHistoricalPrices _).expects(*,*).returning(TestBitCoinInstantPriceListFuture) once()
        (movingAverageCalculatorServiceMock.average _).expects(TestBitCoinPriceList, *).returning(TestExpectedMovingAveragesFuture) once()
        
        val bitCoinPriceMovingAverageService = new 
            SimpleMovingAverageBasedBitCoinPriceMovingAverageService(coinBaseApiServiceMock, movingAverageCalculatorServiceMock)
        val movingAverageFuture = bitCoinPriceMovingAverageService.average(ValidStartDate, ValidEndDate, MovingAverageNumber)

        val movingAverages = Await.result(movingAverageFuture, 1 minute)
        
        movingAverages.size should be(6)
        movingAverages should be(TestExpectedMovingAverages)
      }
    }

    "throw an exception" when {
      "coinBaseApiService throws an exception" in {
        (coinBaseApiServiceMock.getHistoricalPrices _).expects(*,*).returning(Future(throw new Exception)) once()

        val bitCoinPriceMovingAverageService = new
            SimpleMovingAverageBasedBitCoinPriceMovingAverageService(coinBaseApiServiceMock, movingAverageCalculatorServiceMock)
        val movingAverageFuture = bitCoinPriceMovingAverageService.average(ValidStartDate, ValidEndDate, MovingAverageNumber)

        an[Exception] should be thrownBy (Await.result(movingAverageFuture, 1 minute))
      }

      "movingAverageCalculatorService throws an exception" in {
        (coinBaseApiServiceMock.getHistoricalPrices _).expects(*,*).returning(TestBitCoinInstantPriceListFuture) once()
        (movingAverageCalculatorServiceMock.average _).expects(TestBitCoinPriceList, *).returning(Future(throw new Exception))once()

        val bitCoinPriceMovingAverageService = new
            SimpleMovingAverageBasedBitCoinPriceMovingAverageService(coinBaseApiServiceMock, movingAverageCalculatorServiceMock)
        val movingAverageFuture = bitCoinPriceMovingAverageService.average(ValidStartDate, ValidEndDate, MovingAverageNumber)

        an[Exception] should be thrownBy (Await.result(movingAverageFuture, 1 minute))
      }
    }
  }
}
