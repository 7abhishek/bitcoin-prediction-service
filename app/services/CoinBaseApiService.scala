package services

import models.BitCoinInstantPrice
import scala.collection.immutable.Seq
import scala.concurrent.Future


trait CoinBaseApiService {
 def getHistoricalPricesByDuration(duration: String): Future[Seq[BitCoinInstantPrice]]
 def getHistoricalPricesByDate(date: String): Future[BitCoinInstantPrice]
 def getHistoricalPrices(startDate: String, endDate: String): Future[Seq[BitCoinInstantPrice]]
}
