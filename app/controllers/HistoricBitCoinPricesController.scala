package controllers

import javax.inject._
import org.slf4j.LoggerFactory
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import services.CoinBaseApiService
import serializers.JsonSerializers._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HistoricBitCoinPricesController @Inject()(cc: ControllerComponents,
                                                coinBaseApiService: CoinBaseApiService,
                                                configuration: Configuration)
  extends AbstractController(cc) {
  private val logger = LoggerFactory.getLogger(classOf[HistoricBitCoinPricesController])

  def getHistoricalBitCoinPrices(durationOption: Option[String], dateOption: Option[String]) = Action.async {
    implicit request: Request[AnyContent] =>
      durationOption match {
        case Some(duration) =>
          coinBaseApiService.getHistoricalPricesByDuration(duration).map(bitcoinPrices => {
            Ok(Json.toJson(bitcoinPrices))
          }).recover {
            case exception: Exception =>
              logger.error("Exception occurred ", exception)
              InternalServerError(Json.toJson("Error occurred, Please contact service administrator"))
          }
        case None => dateOption match {
          case Some(date) => coinBaseApiService.getHistoricalPricesByDate(date).map(bitcoinPrice => {
            Ok(Json.toJson(bitcoinPrice))
          }).recover {
            case exception: Exception =>
              logger.error("Exception occurred ", exception)
              InternalServerError(Json.toJson("Error occurred, Please contact service administrator"))
          }
          case None => Future(BadRequest("duration and date parameters missing."))
        }
      }
  }
  
  def getHistoricalBitCoinPricesByInterval(startDate: String, endDate:String) = Action.async {
    implicit request: Request[AnyContent] =>
      logger.info("startDate {} endDate {} ", startDate, endDate: Any)
    coinBaseApiService.getHistoricalPrices(startDate, endDate).map(bitCoinPrices => {
      Ok(Json.toJson(bitCoinPrices))
    }).recover {
      case exception: Exception => 
        logger.error("Error occurred during getting historical prices by interval", exception)
        InternalServerError(Json.toJson("Error occurred, Please contact service administrator"))
    } 
  }
}
