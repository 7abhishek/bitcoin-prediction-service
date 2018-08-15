package controllers

import com.google.inject.Inject
import models.BitCoinTradingDecisionStrategy
import org.slf4j.LoggerFactory
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import services.BitCoinTradingDecisionService

class BitCoinTradingDecisionController @Inject()(controllerComponents: ControllerComponents,
                                                 bitCoinTradingDecisionService: BitCoinTradingDecisionService) extends
  AbstractController(controllerComponents) {
  import BitCoinTradingDecisionController._
  private val logger = LoggerFactory.getLogger(classOf[BitCoinTradingDecisionController])

  def getDecision(days: Int = defaultForecastingNumberOfDays, strategy: String = DefaultStrategy) = Action.async {
    implicit request: Request[AnyContent] =>
      logger.info("days {}  strategy {}", days, strategy: Any)
      Try(BitCoinTradingDecisionStrategy.withName(strategy.toLowerCase)) match {
        case Success(value) =>
          bitCoinTradingDecisionService.getDecision(days, value)
            .map(decision => Ok(Json.toJson(decision)))
            .recover {
              case exception: Exception =>
                logger.error("Exception occurred when getDecision", exception)
                InternalServerError("Error occurred, Please contact system administrator")
            }
        case Failure(error) =>
          logger.error("Exception occurred while decision", error)
          Future(InternalServerError("Error occurred , Please contact system administrator"))
      }
  }
}

object BitCoinTradingDecisionController {
  private val defaultForecastingNumberOfDays = 15
  private val DefaultStrategy = "safe"
}
