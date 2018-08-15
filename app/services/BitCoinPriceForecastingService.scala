package services

import scala.concurrent.Future
import scala.collection.immutable.Seq

trait BitCoinPriceForecastingService {
  def forecast(days: Int): Future[Seq[Double]]
}
