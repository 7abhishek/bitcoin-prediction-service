package services

import models.BitCoinInstantPrice
import scala.collection.immutable.Seq
import scala.concurrent.Future


trait CoinBaseApiService {
 def getHistoricalPrices: Future[Seq[BitCoinInstantPrice]]
}
