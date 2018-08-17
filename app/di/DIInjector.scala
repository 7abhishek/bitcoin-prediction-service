package di

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import org.slf4j.LoggerFactory
import services.{BitCoinPriceForecastingService, BitCoinPriceMovingAverageService, BitCoinTradingDecisionService, CoinBaseApiService, MovingAverageCalculatorService}
import services.impl.{HttpBasedCoinBaseApiService, StrategicalBitCoinTradingDecisionService, SimpleARIMABasedBitCoinForecastingService, SimpleMovingAverageBasedBitCoinPriceMovingAverageService, SimpleMovingAverageCalculatorService}

class DIInjector extends AbstractModule {
  private val logger = LoggerFactory.getLogger(classOf[DIInjector])
  
  override def configure(): Unit = {
    logger.info("binding services")
    bind(classOf[CoinBaseApiService]).to(classOf[HttpBasedCoinBaseApiService])
    bind(classOf[BitCoinPriceForecastingService]).annotatedWith(Names.named("ARIMA")).to(classOf[SimpleARIMABasedBitCoinForecastingService])
    bind(classOf[MovingAverageCalculatorService]).annotatedWith(Names.named("SMA")).to(classOf[SimpleMovingAverageCalculatorService])
    bind(classOf[BitCoinPriceMovingAverageService]).to(classOf[SimpleMovingAverageBasedBitCoinPriceMovingAverageService])
    bind(classOf[MovingAverageCalculatorService]).to(classOf[SimpleMovingAverageCalculatorService])
    bind(classOf[BitCoinTradingDecisionService]).to(classOf[StrategicalBitCoinTradingDecisionService])
  }
}
