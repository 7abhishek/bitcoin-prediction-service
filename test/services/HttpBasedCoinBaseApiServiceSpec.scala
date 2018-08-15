package services

import models.BitCoinInstantPrice
import org.joda.time.Instant
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, OneInstancePerTest, WordSpec}
import org.scalatest.concurrent.ScalaFutures
import play.api.{ConfigLoader, Configuration}
import play.api.cache.AsyncCacheApi
import play.api.libs.json.{Json, JsValue}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import scala.collection.immutable.Seq
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag
import services.impl.HttpBasedCoinBaseApiService
import scala.concurrent.duration._
import scala.io.Source

class HttpBasedCoinBaseApiServiceSpec extends WordSpec with MockFactory with Matchers with ScalaFutures with 
  OneInstancePerTest {

  private val configurationMock =  mock[Configuration]
  private val wsClientMock =  mock[WSClient]
  private val cacheApiMock =  mock[AsyncCacheApi]
  private val fakeCoinBaseUrl = "fakeUrl"
  private val TestInstant = Instant.parse("2018-08-16")
  private val TestBitCoinInstantPrice = BitCoinInstantPrice(time = TestInstant, price = "1490.0")
  private val TestBitCoinPriceList = Seq(TestBitCoinInstantPrice)
  private val TestBitCoinInstantPricesFuture = Future(TestBitCoinPriceList)
  private val ValidDuration = "week"
  private val InValidDuration = "lightyear"
  private val ValidDate = "2018-01-02"
  private val TestInstantForValidDate = Instant.parse(ValidDate)
  private val TestBitCoinInstantPriceForValidDate = BitCoinInstantPrice(time = TestInstantForValidDate, price = "1490.0")
  private val TestBitCoinInstantPriceForValidDateFuture = Future(TestBitCoinInstantPriceForValidDate)
  private val TestExpectedResponse = mock[WSResponse]
  private val wsRequestMock = mock[WSRequest]
  private val TestExpectedResponseFuture = Future(TestExpectedResponse)
  private val TestExpectedResponseJson: JsValue = Json.parse(Source.fromURL(getClass.getResource("/testExpectedJsonResponseForValidDate.json")).mkString)
  private val ValidStartDate = "2018-03-01"
  private val ValidEndDate = "2018-03-03"
  private val daysDifferenceBetweenValidDates = 3
  
  
  
  "getHistoricalPricesByDuration" should {
    "return bitcoin prices" when {
      "duration is valid" in {
        (configurationMock.get[String](_: String)(_: ConfigLoader[String])).expects(*, *)
          .returning(fakeCoinBaseUrl) twice()
        (cacheApiMock.getOrElseUpdate[Seq[BitCoinInstantPrice]](_: String, _: Duration)
          (_: Future[Seq[BitCoinInstantPrice]]) (_: ClassTag[Seq[BitCoinInstantPrice]]))
          .expects(*, *, *, *).returns(TestBitCoinInstantPricesFuture) once()
        
        val httpBasedCoinBaseApiService = new HttpBasedCoinBaseApiService(configurationMock, wsClientMock, cacheApiMock)
        val resultFuture = httpBasedCoinBaseApiService.getHistoricalPricesByDuration(ValidDuration)
        
        whenReady(resultFuture) { result =>
          result should be(TestBitCoinPriceList)
        }
      }
    }

    "throw IllegalArguementException" when {
      "duration is invalid" in {
        (configurationMock.get[String](_: String)(_: ConfigLoader[String])).expects(*, *)
          .returning(fakeCoinBaseUrl) twice()

        val httpBasedCoinBaseApiService = new HttpBasedCoinBaseApiService(configurationMock, wsClientMock, cacheApiMock)
        val resultFuture = httpBasedCoinBaseApiService.getHistoricalPricesByDuration(InValidDuration)
        
        an [Exception] should be thrownBy {
          Await.result(resultFuture, 2 minute)
        }
      }
    }
  }
  
  "getHistoricalPricesByDate" should {
    "return bitcoin prices" when {
      "date is valid" in {
        (configurationMock.get[String](_: String)(_: ConfigLoader[String])).expects(*, *)
          .returning(fakeCoinBaseUrl) twice()
        (wsClientMock.url _).expects(*).returning(wsRequestMock) once()
        (wsRequestMock.get _).expects().returning(TestExpectedResponseFuture) once()
        (TestExpectedResponse.status _).expects().returning(200) once()
        (TestExpectedResponse.json _).expects().returning(TestExpectedResponseJson) once()

        val httpBasedCoinBaseApiService = new HttpBasedCoinBaseApiService(configurationMock, wsClientMock, cacheApiMock)
        val resultFuture = httpBasedCoinBaseApiService.getHistoricalPricesByDate(ValidDate)

        whenReady(resultFuture) { result =>
          result should be(TestBitCoinInstantPriceForValidDate)
        }
      }
    }
  }
  
  
  "getHistoricalPrices By Interval" should {
    "return bitcoin prices" when {
      "interval is valid" in {
        (configurationMock.get[String](_: String)(_: ConfigLoader[String])).expects(*, *)
          .returning(fakeCoinBaseUrl) twice()
        (cacheApiMock.getOrElseUpdate[BitCoinInstantPrice](_: String, _: Duration)
          (_: Future[BitCoinInstantPrice]) (_: ClassTag[BitCoinInstantPrice]))
          .expects(*, *, *, *).returns(TestBitCoinInstantPriceForValidDateFuture).repeated(daysDifferenceBetweenValidDates).times

        val httpBasedCoinBaseApiService = new HttpBasedCoinBaseApiService(configurationMock, wsClientMock, cacheApiMock)
        val resultFuture = httpBasedCoinBaseApiService.getHistoricalPrices(ValidStartDate, ValidEndDate)
        val testExpectedBitCoinPricesForInterval = Seq.fill(daysDifferenceBetweenValidDates)(TestBitCoinInstantPriceForValidDate)

        whenReady(resultFuture) { result =>
          println("result "+ result.toList)
          result should be(testExpectedBitCoinPricesForInterval)
        }
      }
    }
  }
}
