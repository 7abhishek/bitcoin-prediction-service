package controllers

import javax.inject._
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._
import services.CoinBaseApiService
import scala.concurrent.ExecutionContext.Implicits.global
import serializers.JsonSerializers._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, coinBaseApiService: CoinBaseApiService, configuration: Configuration)
  extends AbstractController(cc) {
  private val AppNameConfigKey = "app.name"
  private val AppName = configuration.get[String](AppNameConfigKey) 

  /**
    * Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
   Ok(AppName)
  }
  
  def getHistoricalBitCoinPrices() = Action.async { implicit request: Request[AnyContent] =>
    coinBaseApiService.getHistoricalPrices.map(bitcoinPrices => {
      Ok(Json.toJson(bitcoinPrices))
    })
  }
}
