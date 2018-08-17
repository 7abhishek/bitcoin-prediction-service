package controllers

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, OneInstancePerTest, WordSpec}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.util.Random
import services.BitCoinPriceMovingAverageService
import scala.collection.immutable.Seq
import scala.collection.breakOut

class MovingAverageControllerSpec extends WordSpec with MockFactory with Matchers with OneInstancePerTest {
  private val controllerComponentsStub = stubControllerComponents()
  private val bitCoinPriceMovingAverageServiceMock = mock[BitCoinPriceMovingAverageService]
  private val ValidStartDate = "2018-01-01"
  private val ValidEndDate = "2018-01-15"
  private val MovingAverageNumber = 5
  private val TestMovingAverageSeed = 1220.45
  private val TestExpectedMovingAverage = getTestMovingAverage
  private val TestExpectedMovingAverageFuture = Future(TestExpectedMovingAverage)
  private val FakeRequestGetMovingAverage = FakeRequest(GET, "/v1/movingaverage")
  private val TestExpectedMovingAverageResponseString = Source.fromURL(getClass.getResource("/testExpectedMovingAverageResponse.json")).mkString

  "movingAverage" should {
    "return valid moving averages" when {
      "startDate and endDates are valid" in {
        (bitCoinPriceMovingAverageServiceMock.average _).expects(ValidStartDate, ValidEndDate, MovingAverageNumber).returning(TestExpectedMovingAverageFuture) once()

        val movingAverageController = new MovingAverageController(controllerComponentsStub, bitCoinPriceMovingAverageServiceMock)
        val movingAverageFuture = movingAverageController.movingAverage(ValidStartDate, ValidEndDate, MovingAverageNumber).apply(FakeRequestGetMovingAverage)

        status(movingAverageFuture) should be(200)
        contentAsString(movingAverageFuture) should be(TestExpectedMovingAverageResponseString)
      }
    }
    
    "return BadRequest" when {
      "startDate is empty" in {
        
        val movingAverageController = new MovingAverageController(controllerComponentsStub, bitCoinPriceMovingAverageServiceMock)
        val movingAverageFuture = movingAverageController.movingAverage("", ValidEndDate, MovingAverageNumber).apply(FakeRequestGetMovingAverage)

        status(movingAverageFuture) should be(400)
      }

      "endDate is empty" in {

        val movingAverageController = new MovingAverageController(controllerComponentsStub, bitCoinPriceMovingAverageServiceMock)
        val movingAverageFuture = movingAverageController.movingAverage(ValidStartDate, "", MovingAverageNumber).apply(FakeRequestGetMovingAverage)

        status(movingAverageFuture) should be(400)
      }

      "movingAverageNumber is less than or equal to 0" in {

        val movingAverageController = new MovingAverageController(controllerComponentsStub, bitCoinPriceMovingAverageServiceMock)
        val movingAverageFuture = movingAverageController.movingAverage(ValidStartDate, ValidEndDate, 0).apply(FakeRequestGetMovingAverage)

        status(movingAverageFuture) should be(400)
      }
    }
    
    "return InternalServerError" when {
      "bitCoinPriceMovingAverageService throws an Exception" in {
        (bitCoinPriceMovingAverageServiceMock.average _).expects(*, *, *).returning(Future(throw new Exception)) once()
        
        val movingAverageController = new MovingAverageController(controllerComponentsStub, bitCoinPriceMovingAverageServiceMock)
        val movingAverageFuture = movingAverageController.movingAverage(ValidStartDate, ValidEndDate, MovingAverageNumber).apply(FakeRequestGetMovingAverage)

        status(movingAverageFuture) should be(500)
      }
    }
  }

  private def getTestMovingAverage: Seq[Double] = {
    (1 to 11).map(count => TestMovingAverageSeed + count)(breakOut)
  }
}
