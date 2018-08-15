package services.impl

import com.cloudera.sparkts.models.{ARIMA, ARIMAModel}
import com.google.inject.Inject
import models.DurationSpan
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.slf4j.LoggerFactory
import scala.collection.immutable.Seq
import scala.concurrent.Future
import services.{BitCoinPriceForecastingService, CoinBaseApiService}
import scala.concurrent.ExecutionContext.Implicits.global

class SimpleARIMABasedBitCoinForecastingService @Inject()(coinBaseApiService: CoinBaseApiService) extends BitCoinPriceForecastingService {
  private val logger = LoggerFactory.getLogger(classOf[SimpleARIMABasedBitCoinForecastingService])
  private val ArimaParameterAutoRegressiveOrder = 1
  private val ArimaParameterDifferencingOrder = 0
  private val ArimaParameterMovingAverageOrder = 7
  private val defaultDurationSpan = DurationSpan.Year
  
  override def forecast(days: Int): Future[Seq[Double]] = {
    fitHistoricalDataToArimaModel().map {
      case (arimaModel, dataArray) =>
        logger.info("coefficients: {}" , arimaModel.coefficients.mkString(","))
        val forecast = arimaModel.forecast(dataArray, days).toArray.takeRight(days)
        logger.info("forecast for next {} days {}", days, forecast:Any)
        forecast.toList
      case _ => throw new Exception("Could not fit historical data to ARIMA Model")
    }
  }
  
  private def fitHistoricalDataToArimaModel(arimaParameterMovingAverageOrder: Int = ArimaParameterMovingAverageOrder,
                                            durationSpan:DurationSpan.Value = defaultDurationSpan): Future[(ARIMAModel, Vector)] = {
    coinBaseApiService.getHistoricalPricesByDuration(durationSpan.toString).map(historicalPrices => {
      val array = Vectors.dense(historicalPrices.map(_.numericalPrice).toArray)
      val arimaModel = ARIMA.fitModel(ArimaParameterAutoRegressiveOrder,
        ArimaParameterDifferencingOrder,
        arimaParameterMovingAverageOrder,
        array)
      (arimaModel, array)
    })
  }
  
}
