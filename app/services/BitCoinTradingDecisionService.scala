package services

import models.{BitCoinTradingDecision, BitCoinTradingDecisionStrategy}
import scala.concurrent.Future

trait BitCoinTradingDecisionService {
  def getDecision(days: Int, decisionStrategy: BitCoinTradingDecisionStrategy.Value): Future[BitCoinTradingDecision.Value]
}
