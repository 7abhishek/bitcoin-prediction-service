package controllers

import javax.inject._
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, configuration: Configuration) extends AbstractController(cc) {
  import HomeController._
  private val AppName = configuration.get[String](AppNameConfigKey)
  private val HealthEndPointMessage = s"$AppName is healthy"
  
  def health() = Action { implicit request: Request[AnyContent] =>
   Ok(Json.toJson(HealthEndPointMessage))
  }
}

object HomeController {
  private val AppNameConfigKey = "app.name"
}
