package services.impl

import com.google.inject.Inject
import com.google.inject.name.Named
import models.{BitCoinTradingDecision, BitCoinTradingDecisionStrategy}
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import scala.concurrent.ExecutionContext.Implicits.global
import org.slf4j.LoggerFactory
import scala.concurrent.Future
import services.{BitCoinPriceForecastingService, BitCoinTradingDecisionService, CoinBaseApiService}

class StrategicalBitCoinTradingDecisionService @Inject()(@Named("ARIMA") 
                                                         bitCoinForecastingService: BitCoinPriceForecastingService,
                                                         coinBaseApiService: CoinBaseApiService) extends BitCoinTradingDecisionService {
  import StrategicalBitCoinTradingDecisionService._
  private val logger = LoggerFactory.getLogger(classOf[StrategicalBitCoinTradingDecisionService])
  
  override def getDecision(days: Int = DefaultForecastingNumberOfDays, decisionStrategy: BitCoinTradingDecisionStrategy.Value = DefaultBitCoinTradingDecisionStrategy)
  : Future[BitCoinTradingDecision.Value] = {
    bitCoinForecastingService.forecast(days).flatMap(forecastedPrice => {
      coinBaseApiService.getHistoricalPricesByDate(Today).map(currentBitCoinInstantPrice => {
        val latestForecastedPrice = forecastedPrice.last
        val currentBTCPrice = currentBitCoinInstantPrice.numericalPrice
        val differenceInPrice = latestForecastedPrice - currentBTCPrice 
        logger.info("currentBTCPrice {} , latestForecastedPrice {} , differenceInPrice : {}",
          currentBTCPrice.toString, latestForecastedPrice.toString, differenceInPrice.toString)
        getStrategicalDecision(decisionStrategy, differenceInPrice, currentBTCPrice)
      })
    })
  }
  
  private def getStrategicalDecision(decisionStrategy: BitCoinTradingDecisionStrategy.Value, 
                                      differenceInPrice: Double, currentBTCPrice: Double) = {
    decisionStrategy match {
      case BitCoinTradingDecisionStrategy.Optimistic =>
        differenceInPrice match {
          case moreThanZero if(moreThanZero > 0) => BitCoinTradingDecision.Buy
          case _ => BitCoinTradingDecision.Hold
        }
      case BitCoinTradingDecisionStrategy.Safe =>
        val percentageDifference  = differenceInPrice / currentBTCPrice
        logger.info("Percentage Difference for currentBTCPrice {}, differenceInPrice {} is {}",
          currentBTCPrice.toString, differenceInPrice.toString, percentageDifference.toString)
        percentageDifference match {
          case moreThanFivePercent if moreThanFivePercent >= FivePercentInDecimal => BitCoinTradingDecision.Buy
          case moreThanTwoPercent if moreThanTwoPercent >= TwoPercentInDecimal && moreThanTwoPercent < FivePercentInDecimal => BitCoinTradingDecision.Hold
          case lessThanTwoPercent if lessThanTwoPercent < TwoPercentInDecimal => BitCoinTradingDecision.Sell
        }
    } 
  }
}

object StrategicalBitCoinTradingDecisionService {
  private val DefaultDateFormat = DateTimeFormat.forPattern("yyyy-MM-dd")
  private val Today = Instant.now.toString(DefaultDateFormat)
  private val DefaultBitCoinTradingDecisionStrategy = BitCoinTradingDecisionStrategy.Safe
  private val DefaultForecastingNumberOfDays = 15
  private val FivePercentInDecimal =  .05
  private val TwoPercentInDecimal =  .02
}
