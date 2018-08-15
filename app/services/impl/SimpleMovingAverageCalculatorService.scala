package services.impl

import com.google.inject.Singleton
import org.slf4j.LoggerFactory
import scala.collection.immutable.Seq
import scala.concurrent.Future
import services.MovingAverageCalculatorService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.breakOut

@Singleton
class SimpleMovingAverageCalculatorService extends MovingAverageCalculatorService {
  private val logger = LoggerFactory.getLogger(classOf[SimpleMovingAverageCalculatorService])

  override def average(data: Seq[Double], movingAverageNumber: Double): Future[Seq[Double]] = Future {
    val movingAvergageInteger = movingAverageNumber.toInt
    val movingAverageSize = (data.size - movingAvergageInteger + 1)
    (0 to movingAverageSize - 1).map(startingIndex => {
      val endingIndex = startingIndex + movingAvergageInteger
      val slicedArray =  data.slice(startingIndex, endingIndex)
      logger.info("slicedArray {} startingIndex {}  endingIndex {}", slicedArray.toString, startingIndex.toString, 
        endingIndex.toString)
      slicedArray.sum / movingAverageNumber
    })(breakOut)
  }
}
