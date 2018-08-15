package services

import org.scalatest.{Matchers, WordSpec}
import services.impl.SimpleMovingAverageCalculatorService
import scala.collection.immutable.Seq
import scala.concurrent.{Await}
import scala.concurrent.duration._

class SimpleMovingAverageCalculatorServiceSpec extends WordSpec with Matchers {
  
  private val TestDataSize = 10
  private val TestDataValue = 1224.5
  private val ValidDataForAveraging = getTestDataForAveraging
  private val InValidDataForAveraging = Seq.empty
  private val MovingAverageNumber = 7
  private val TestAwaitDuration = 1 minute
  private val TestExpectedMovingAverage = Seq(1228.5, 1229.5, 1230.5, 1231.5)
  
  "average" should {
    "return valid averages" when {
      "data and movingAveragenumber are valid" in{
        val simpleMovingAverageCalculatorService = new SimpleMovingAverageCalculatorService
        
        val movingAverageFuture = simpleMovingAverageCalculatorService.average(ValidDataForAveraging, MovingAverageNumber)
        val movingAverage = Await.result(movingAverageFuture, TestAwaitDuration)

        movingAverage.size  should be (4)
        movingAverage should be(TestExpectedMovingAverage)
      }
    }
  }
  
  private def getTestDataForAveraging: Seq[Double] = {
    (1 to TestDataSize).map(index => {
      TestDataValue + index
    })
  }
}
