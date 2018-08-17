package controllers

import com.google.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import services.BitCoinPriceMovingAverageService
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class MovingAverageController @Inject()(controllerComponents: ControllerComponents,
                                        bitCoinPriceMovingAverageService: BitCoinPriceMovingAverageService) extends AbstractController(controllerComponents) {
  private val logger = LoggerFactory.getLogger(classOf[BitCoinPriceForecastingController])

  def movingAverage(startDate: String, endDate: String, movingAverageNumber: Double) = Action.async {
    implicit request: Request[AnyContent] =>
      Try {
        require(startDate.nonEmpty, "startDate cannot be empty")
        require(endDate.nonEmpty, "endDate cannot be empty")
        require(movingAverageNumber > 0.0, "movingAverageOrder cannot be less than or equal to 0")
      } match {
        case Success(_) =>
          bitCoinPriceMovingAverageService.average(startDate, endDate, movingAverageNumber).map(movingAverage => {
            Ok(Json.toJson(movingAverage))
          }).recover {
            case exception: Exception =>
              logger.error("Exception occurred during moving average", exception)
              InternalServerError(Json.toJson("Error occurred, Please contact service administrator"))
          }
        case Failure(error) =>
          logger.error(s"Exception occurred while forecasting, $error")
          Future(BadRequest(error.getMessage))
      }
  }
}
