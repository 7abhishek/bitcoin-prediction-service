package controllers

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, OneInstancePerTest, WordSpec}
import org.scalatest.concurrent.ScalaFutures
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scala.collection.immutable.Seq
import scala.concurrent.Future
import services.BitCoinPriceForecastingService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source

class BitCoinPriceForecastingControllerSpec extends WordSpec with MockFactory with Matchers with OneInstancePerTest {
  
  private val controllerComponentsStub = stubControllerComponents()
  private val bitCoinPriceForecastingServiceMock = mock[BitCoinPriceForecastingService]
  private val ValidForecastDays = 15
  private val ForecastDaysExceedingUpperBoundLimit = 16
  private val TestBitCoinPrice = 1490.0
  private val TestBitCoinPriceList = Seq.fill(ValidForecastDays)(TestBitCoinPrice)
  private val TestBitCoinPriceFuture = Future(TestBitCoinPriceList)
  private val FakeRequestGetForecastedBitCoinPrices = FakeRequest(GET, "/v1/forecast")
  private def TestExpectedForecastedResponseString = Source.fromURL(getClass.getResource("/testExpectedForecastedBitCoinPrices.json")).mkString
  
  
  "forecast" should {
    "return forecasted values" when {
     "number of days to be forecasted is valid" in {
       (bitCoinPriceForecastingServiceMock.forecast _).expects(*).returning(TestBitCoinPriceFuture)
       
       val bitCoinPriceForecastingController = new BitCoinPriceForecastingController(controllerComponentsStub, bitCoinPriceForecastingServiceMock)
       val forecastFuture = bitCoinPriceForecastingController.forecast(ValidForecastDays).apply(FakeRequestGetForecastedBitCoinPrices)
       
       status(forecastFuture) should be(200)
       contentAsString(forecastFuture) should be(TestExpectedForecastedResponseString)
     }
    }
    
    "return BadRequest" when {
      "number of days to be forecasted exceeds upperbound limit" in {

        val bitCoinPriceForecastingController = new BitCoinPriceForecastingController(controllerComponentsStub, bitCoinPriceForecastingServiceMock)
        val forecastFuture = bitCoinPriceForecastingController.forecast(ForecastDaysExceedingUpperBoundLimit).apply(FakeRequestGetForecastedBitCoinPrices)

        status(forecastFuture) should be(400)
      }
    }

    "return InternalServerError" when {
      "bitCoinPriceForecastingService throws an exception" in {
        (bitCoinPriceForecastingServiceMock.forecast _).expects(*).returning(Future(throw new Exception))

        val bitCoinPriceForecastingController = new BitCoinPriceForecastingController(controllerComponentsStub, bitCoinPriceForecastingServiceMock)
        val forecastFuture = bitCoinPriceForecastingController.forecast(ValidForecastDays).apply(FakeRequestGetForecastedBitCoinPrices)

        status(forecastFuture) should be(500)
      }
    }
  }
}
