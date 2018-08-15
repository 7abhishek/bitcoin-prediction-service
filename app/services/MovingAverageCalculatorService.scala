package services

import scala.collection.immutable.Seq
import scala.concurrent.Future

trait MovingAverageCalculatorService {
    def average(data: Seq[Double], movingAverageNumber: Double): Future[Seq[Double]]
}
