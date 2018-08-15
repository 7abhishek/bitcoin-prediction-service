package services

import scala.collection.immutable.Seq
import scala.concurrent.Future

trait BitCoinPriceMovingAverageService {
  def average(startDate: String, endDate: String, movingAverageNumber: Double): Future[Seq[Double]]
}
