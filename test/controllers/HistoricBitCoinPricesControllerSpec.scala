package controllers

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import models.BitCoinInstantPrice
import org.joda.time.Instant
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures
import play.api.Configuration
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scala.collection.immutable.Seq
import scala.concurrent.Future
import services.CoinBaseApiService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source


class HistoricBitCoinPricesControllerSpec extends WordSpec with MockFactory with Matchers with ScalaFutures {

  private val controllerComponentsMock = stubControllerComponents()
  private val coinBaseApiServiceMock = mock[CoinBaseApiService]
  private val configurationMock = mock[Configuration]
  private val ValidDurationWeek = "week"
  private val ValidDurationWeekOption = Some(ValidDurationWeek)
  private val ValidDate = "2018-01-02"
  private val ValidDateOption = Some(ValidDate)
  private val ValidStartDate = "2018-01-01"
  private val ValidEndDate = "2018-02-01"
  private val historicBitCoinPricesController = new HistoricBitCoinPricesController(controllerComponentsMock,
    coinBaseApiServiceMock,
    configurationMock)
  private val TestInstant = Instant.parse("2018-08-16")
  private val TestBitCoinInstantPrice = BitCoinInstantPrice(time = TestInstant, price = "1490.0")
  private val TestBitCoinPriceList = Seq(TestBitCoinInstantPrice)
  private val TestBitCoinInstantPricesFuture = Future(TestBitCoinPriceList)
  private val TestBitCoinInstantPriceFuture = Future(TestBitCoinInstantPrice)
  private val FakeRequestGetHistoricalPrices = FakeRequest(GET, "/historicalprices")
  private val FakeRequestGetHistoricalPricesByInterval = FakeRequest(GET, "/historicalpricesbyinterval")
  private def TestExpectedStringGetHistoricalPricesForDuration = Source.fromURL(getClass.getResource("/testExpectedGetHistoricPricesForDuration.json"))
  private def TestExpectedStringGetHistoricalPricesByDate = Source.fromURL(getClass.getResource("/testExpectedGetHistoricPricesByDate.json"))


  "getHistoricalBitCoinPrices " should {
    "return bitcoin prices for duration" when {
      "a vaid duration is passed as parameter" in {
        (coinBaseApiServiceMock.getHistoricalPricesByDuration _).expects(*).returning(TestBitCoinInstantPricesFuture)once

        val result = historicBitCoinPricesController.getHistoricalBitCoinPrices(ValidDurationWeekOption, None).apply(FakeRequestGetHistoricalPrices)
        
        status(result) should be(200)
        contentAsString(result) should be (TestExpectedStringGetHistoricalPricesForDuration.mkString)
      }      
      
      "a valid date is passed as parameter" in {
        (coinBaseApiServiceMock.getHistoricalPricesByDate _).expects(*).returning(TestBitCoinInstantPriceFuture)once

        val result = historicBitCoinPricesController.getHistoricalBitCoinPrices(None, ValidDateOption).apply(FakeRequestGetHistoricalPrices)

        status(result) should be(200)
        contentAsString(result) should be (TestExpectedStringGetHistoricalPricesByDate.mkString)
      }
    }
    
    "return InternalServer error" when {
      "coinBaseApiService throws an exception and parameter is valid duration" in {
        (coinBaseApiServiceMock.getHistoricalPricesByDuration _).expects(*).returning(Future(throw new IllegalArgumentException))once

        val result = historicBitCoinPricesController.getHistoricalBitCoinPrices(ValidDurationWeekOption, None).apply(FakeRequestGetHistoricalPrices)

        status(result) should be(500)
      }

      "coinBaseApiService throws an exception and parameter is valid date" in {
        (coinBaseApiServiceMock.getHistoricalPricesByDate _).expects(*).returning(Future(throw new IllegalArgumentException))once

        val result = historicBitCoinPricesController.getHistoricalBitCoinPrices(None, ValidDateOption).apply(FakeRequestGetHistoricalPrices)

        status(result) should be(500)
      }
    }
    
    "returns BadRequest status" when {
      "empty date and empty duration is passed as parameter" in {

        val result = historicBitCoinPricesController.getHistoricalBitCoinPrices(None, None).apply(FakeRequestGetHistoricalPrices)

        status(result) should be(400)
      }
    }
  }
  
  "getHistoricalBitCoinPricesByInterval" should {
    "return bitcoin prices" when {
      "the interval is valid" in {
        (coinBaseApiServiceMock.getHistoricalPrices _).expects(*,*).returning(TestBitCoinInstantPricesFuture) once
        
        val result = historicBitCoinPricesController.getHistoricalBitCoinPricesByInterval(ValidStartDate, ValidEndDate).apply(FakeRequestGetHistoricalPricesByInterval)

        status(result) should be(200)
        contentAsString(result) should be (TestExpectedStringGetHistoricalPricesForDuration.mkString)
      }
    }
    
    "return InternalServerError" when {
      "coinBaseApiService throws Exception" in {
        (coinBaseApiServiceMock.getHistoricalPrices _).expects(*, *).returning(Future(throw new IllegalArgumentException)) once

        val result = historicBitCoinPricesController.getHistoricalBitCoinPricesByInterval(ValidStartDate, ValidEndDate).apply(FakeRequestGetHistoricalPricesByInterval)

        status(result) should be(500)
      }
    }
  }
}
