package controllers

import com.google.inject.{Inject, Singleton}
import com.google.inject.name.Named
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import services.BitCoinPriceForecastingService

@Singleton
class BitCoinPriceForecastingController @Inject()(controllerComponents: ControllerComponents,
                                                  @Named("ARIMA") bitCoinPriceForecastingService: BitCoinPriceForecastingService)  
extends AbstractController(controllerComponents) {
  private val ForecastingDaysLowerBound = 0
  private val ForecastingDaysUpperBound = 15
  private val logger = LoggerFactory.getLogger(classOf[BitCoinPriceForecastingController])

  def forecast(days:Int) = Action.async { implicit request: Request[AnyContent] =>
    Try(require(days > ForecastingDaysLowerBound && days <= ForecastingDaysUpperBound, s"days should lie between " +
      s"$ForecastingDaysLowerBound and $ForecastingDaysUpperBound (inclusive), but was $days")) match {
      case Success(_) =>
        bitCoinPriceForecastingService.forecast(days).map(forecastedPrice => {
          Ok(Json.toJson(forecastedPrice))
        }).recover{
          case exception:Exception =>
            logger.error(s"Exception occurred during forecast for $days days", exception)
            InternalServerError("Error occurred, Please contact system administrator")
        }
      case Failure(error) =>
        logger.error("Exception occurred while forecasting", error)
        Future(BadRequest)
    }
  }
}
