package controllers

import models.{BitCoinTradingDecision, BitCoinTradingDecisionStrategy}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scala.concurrent.Future
import services.BitCoinTradingDecisionService
import scala.concurrent.ExecutionContext.Implicits.global

class BitCoinTradingDecisionControllerSpec extends WordSpec with MockFactory with Matchers {
  private val controllerComponentsMock = stubControllerComponents()
  private val bitCoinTradingDecisionServiceMock = mock[BitCoinTradingDecisionService]
  private val FakeRequestGetMovingAverage = FakeRequest(GET, "/decision")
  private val TestBitCoinTradingDecision = BitCoinTradingDecision.Hold
  private val TestExpectedDecisionResponseString = s"""\"${TestBitCoinTradingDecision.toString}\""""
  private val TestBitCoinTradingDecisionFuture = Future(TestBitCoinTradingDecision)
  private val InvalidStrategy = "spendthrift"
  
  
  "getDecision" should {
    "return an Ok Result" when {
      "bitCoinTradingDecisionService returns a valid response" in {
        (bitCoinTradingDecisionServiceMock.getDecision _).expects(*, BitCoinTradingDecisionStrategy.Safe).returning(TestBitCoinTradingDecisionFuture) once()
        
        val bitCoinTradingDecisionController = new BitCoinTradingDecisionController(controllerComponentsMock, bitCoinTradingDecisionServiceMock)
        val decisionResponseFuture = bitCoinTradingDecisionController.getDecision()
          .apply(FakeRequestGetMovingAverage)
        
        status(decisionResponseFuture) should be(200)
        contentAsString(decisionResponseFuture) should be(TestExpectedDecisionResponseString)
      }
    }

    "return an InternalServerError" when {
      "invalid strategy is passed as parameter" in {
        val bitCoinTradingDecisionController = new BitCoinTradingDecisionController(controllerComponentsMock, bitCoinTradingDecisionServiceMock)
        val decisionResponseFuture = bitCoinTradingDecisionController.getDecision(strategy = InvalidStrategy)
          .apply(FakeRequestGetMovingAverage)

        status(decisionResponseFuture) should be(500)
      }

      "bitCoinTradingDecisionService throws an Exception" in {
        (bitCoinTradingDecisionServiceMock.getDecision _).expects(*, *).returning(Future(throw new Exception)) once()
        
        val bitCoinTradingDecisionController = new BitCoinTradingDecisionController(controllerComponentsMock, bitCoinTradingDecisionServiceMock)
        val decisionResponseFuture = bitCoinTradingDecisionController.getDecision()
          .apply(FakeRequestGetMovingAverage)

        status(decisionResponseFuture) should be(500)
      }
    }
  }
}
