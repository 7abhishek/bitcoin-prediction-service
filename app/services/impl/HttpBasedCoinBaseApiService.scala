package services.impl

import com.google.inject.Inject
import models.BitCoinInstantPrice
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.libs.json.JsResult
import play.api.libs.ws.WSClient
import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import serializers.JsonSerializers._
import services.CoinBaseApiService

class HttpBasedCoinBaseApiService @Inject()(configuration: Configuration, wsClient: WSClient, cacheApi: AsyncCacheApi) 
  extends 
  CoinBaseApiService {
  private val CoinBaseHistoricUrlConfigKey = "coinbase.historic.data.url"
  private val CoinBaseHistoricDataUrl = configuration.get[String](CoinBaseHistoricUrlConfigKey)
  private val logger = LoggerFactory.getLogger(classOf[HttpBasedCoinBaseApiService])
  private val Now  = DateTime.now()
  private val TwoDigitMonth = "%02d".format(Now.getMonthOfYear)
  private val TodayString = s"${Now.getDayOfMonth}${TwoDigitMonth}${Now.getYear}"
  private val CacheExpiryDuration = 1 day
  
  override def getHistoricalPrices: Future[Seq[BitCoinInstantPrice]] = {
    logger.info("trying to retrieve data from cache for {}", TodayString)
    cacheApi.getOrElseUpdate[Seq[BitCoinInstantPrice]](TodayString) {
      logger.info("cache is invalid, retrieving latest data from coinbase api")
      getLatestHistoricalPrices
    }
  }
  
  private def getLatestHistoricalPrices: Future[Seq[BitCoinInstantPrice]] = {
    wsClient.url(CoinBaseHistoricDataUrl).get().map(response => {
      val prices = response.json \ "data" \ "prices"
      prices match {
        case validJson =>
          validJson.validate[Seq[BitCoinInstantPrice]] match {
            case bitCoinPrices: JsResult[Seq[BitCoinInstantPrice]] =>
              bitCoinPrices.get
            case error => throw new Exception(s" error $error")
          }
        case error =>
          throw new Exception(s" error $error")
      }
    })
  }
}
