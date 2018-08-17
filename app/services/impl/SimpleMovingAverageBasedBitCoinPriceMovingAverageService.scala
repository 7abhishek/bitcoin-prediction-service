package services.impl

import com.google.inject.Inject
import com.google.inject.name.Named
import scala.collection.immutable.Seq
import scala.concurrent.Future
import services.{BitCoinPriceMovingAverageService, CoinBaseApiService, MovingAverageCalculatorService}
import scala.concurrent.ExecutionContext.Implicits.global

class SimpleMovingAverageBasedBitCoinPriceMovingAverageService @Inject()(coinBaseApiService: CoinBaseApiService,
                                                                         @Named("SMA")
                                                                         movingAverageCalculatorService: MovingAverageCalculatorService) 
  extends BitCoinPriceMovingAverageService {
  import SimpleMovingAverageBasedBitCoinPriceMovingAverageService._
  override def average(startDate: String, endDate: String, movingAverageNumber: Double = DefaultMovingAverageNumber): Future[Seq[Double]] = {
    coinBaseApiService.getHistoricalPrices(startDate, endDate).flatMap(historicalBitCoinPrices => {
      movingAverageCalculatorService.average(historicalBitCoinPrices.map(_.numericalPrice), movingAverageNumber)
    })
  }
}

object SimpleMovingAverageBasedBitCoinPriceMovingAverageService {
  private val DefaultMovingAverageNumber = 7.0
}