package controllers

import org.scalamock.scalatest.MockFactory
import org.scalatest.OneInstancePerTest
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.{ConfigLoader, Configuration}
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 *
 * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
 */
class HomeControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with MockFactory with OneInstancePerTest {
  private val configurationMock = mock[Configuration]
  private val AppName = "bitcoin-prediction-service"
  private val HomePageResponseHeaderContentType = "application/json"
  private val ExpectedConfigKeyCalled = "app.name"


  "HomeController GET" should {

    "render the index page from a new instance of controller" in {
      (configurationMock.get[String](_: String)(_: ConfigLoader[String])).expects(*, *).returning(AppName) once

      val controller = new HomeController(stubControllerComponents(), configurationMock)
      val home = controller.index().apply(FakeRequest(GET, "/"))

      status(home) mustBe OK
      contentType(home) mustBe Some(HomePageResponseHeaderContentType)
      contentAsString(home) must include(AppName)
    }
  }
}