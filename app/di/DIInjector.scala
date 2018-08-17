package di

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import org.slf4j.LoggerFactory
import services.{BitCoinPriceForecastingService, BitCoinPriceMovingAverageService, CoinBaseApiService, MovingAverageCalculatorService}
import services.impl.{HttpBasedCoinBaseApiService, SimpleARIMABasedBitCoinForecastingService, SimpleMovingAverageBasedBitCoinPriceMovingAverageService, SimpleMovingAverageCalculatorService}

class DIInjector extends AbstractModule {
  private val logger = LoggerFactory.getLogger(classOf[DIInjector])
  
  override def configure(): Unit = {
    logger.info("binding services")
    bind(classOf[CoinBaseApiService]).to(classOf[HttpBasedCoinBaseApiService])
    bind(classOf[BitCoinPriceForecastingService]).annotatedWith(Names.named("ARIMA")).to(classOf[SimpleARIMABasedBitCoinForecastingService])
    bind(classOf[MovingAverageCalculatorService]).to(classOf[SimpleMovingAverageCalculatorService])
    bind(classOf[BitCoinPriceMovingAverageService]).annotatedWith(Names.named("SMA")).to(classOf[SimpleMovingAverageBasedBitCoinPriceMovingAverageService])
  }
}
