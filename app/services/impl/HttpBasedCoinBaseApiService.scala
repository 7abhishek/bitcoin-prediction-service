package services.impl

import com.google.inject.{Inject, Singleton}
import models.{BitCoinInstantPrice, DurationSpan}
import org.joda.time.{DateTime, Days, Instant}
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory
import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.libs.json.{JsDefined, JsResult, JsString, JsUndefined}
import play.api.libs.ws.{WSClient, WSResponse}
import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import serializers.JsonSerializers._
import services.CoinBaseApiService

@Singleton
class HttpBasedCoinBaseApiService @Inject()(configuration: Configuration, wsClient: WSClient, cacheApi: AsyncCacheApi)
  extends CoinBaseApiService {
  private val CoinBaseHistoricUrlConfigKey = "coinbase.historic.price.data.url"
  private val CoinBaseSpotPriceUrlConfigKey = "coinbase.spot.price.date.url"
  private val CoinBaseHistoricDataUrl = configuration.get[String](CoinBaseHistoricUrlConfigKey)
  private val CoinBaseSpotPriceByDateUrl = configuration.get[String](CoinBaseSpotPriceUrlConfigKey)
  private val logger = LoggerFactory.getLogger(classOf[HttpBasedCoinBaseApiService])
  private val Now = DateTime.now()
  private val TwoDigitMonth = "%02d".format(Now.getMonthOfYear)
  private val TodayString = s"${Now.getDayOfMonth}${TwoDigitMonth}${Now.getYear}"
  private val CacheExpiryDuration = 1 day
  private val CacheExpiryDurationForInterval = 1 hour
  private val DefaultDateFormatPattern = "yyyy-MM-dd"
  private val DefaultDateFormatter = DateTimeFormat.forPattern(DefaultDateFormatPattern)

  override def getHistoricalPricesByDuration(duration: String): Future[Seq[BitCoinInstantPrice]] = {
    val durationSpanTry = Future(Try(DurationSpan.withName(duration.toLowerCase())))
    durationSpanTry flatMap {
      case Success(durationSpan) =>
        val cacheKey = s"$TodayString-${durationSpan.toString}"
        logger.info("trying to retrieve data from cache for {}", cacheKey)
        cacheApi.getOrElseUpdate[Seq[BitCoinInstantPrice]](cacheKey, CacheExpiryDuration) {
          logger.info("cache is invalid, retrieving latest data from coinbase api")
          getLatestHistoricalPrices(durationSpan)
        }
      case Failure(error) =>
        throw error
    }
  }

  override def getHistoricalPricesByDate(date: String): Future[BitCoinInstantPrice] = {
    val CoinBaseSpotPriceByDateFullUrl = s"$CoinBaseSpotPriceByDateUrl$date"
    val dateTimeInstant = Instant.parse(date, DefaultDateFormatter)
    makeWSGetRequest(CoinBaseSpotPriceByDateFullUrl).map(response => {
      logger.info("response status {}", response.status)
      val amount  = response.json \ "data" \ "amount"
      amount match {
        case JsDefined(amount) => BitCoinInstantPrice(time = dateTimeInstant, price = amount.as[JsString].value)
        case error: JsUndefined =>
          throw new Exception(s" error $error")
      }
    })
  }

  override def getHistoricalPrices(startDate: String, endDate: String): Future[Seq[BitCoinInstantPrice]] = {
    val startDateTime = DateTime.parse(startDate, DefaultDateFormatter)
    val endDateTime = DateTime.parse(endDate, DefaultDateFormatter)
    require(startDateTime.isBefore(endDateTime), s"startDate should be before endDate, but was startDate $startDate ," +
      s" endDate $endDate")
    val numberOfDays = Days.daysBetween(startDateTime.toLocalDate, endDateTime.toLocalDate).getDays
    logger.info("numberOfDays between {} and {} is {}",startDate, endDate, numberOfDays.toString)
    Future.sequence((0 to numberOfDays).map(dayCount => {
      val latestDate = startDateTime.plusDays(dayCount)
      val date = latestDate.toString(DefaultDateFormatter)
      cacheApi.getOrElseUpdate[BitCoinInstantPrice](date, CacheExpiryDurationForInterval) {
        getHistoricalPricesByDate(date)
      }
    }).toList)
  }

  private def getLatestHistoricalPrices(durationSpan: DurationSpan.Value): Future[Seq[BitCoinInstantPrice]] = {
        val duration = durationSpan.toString
        val CoinBaseFullUrl = s"$CoinBaseHistoricDataUrl$duration"
        getBitCoinHistoricalPricesFromExternalApi(CoinBaseFullUrl)
  }

  private def getBitCoinHistoricalPricesFromExternalApi(CoinBaseFullUrl: String): Future[Seq[BitCoinInstantPrice]] = {
    makeWSGetRequest(CoinBaseFullUrl).map(response => {
      val prices = response.json \ "data" \ "prices"
      prices match {
        case JsDefined(validJson) =>
          validJson.validate[Seq[BitCoinInstantPrice]] match {
            case bitCoinPrices: JsResult[Seq[BitCoinInstantPrice]] =>
              bitCoinPrices.get
            case error => throw new Exception(s"Error during Deserialization $error")
          }
        case error: JsUndefined =>
          throw new Exception(s"Error during Deserialization $error")
      }
    })
  }
  
  private def makeWSGetRequest(url: String): Future[WSResponse] = {
    logger.info("making external WS request with url {}", url)
    wsClient.url(url).get
  }
}
