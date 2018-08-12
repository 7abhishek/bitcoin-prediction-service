package di

import com.google.inject.AbstractModule
import org.slf4j.LoggerFactory
import services.{CoinBaseApiService, HttpBasedCoinBaseApiService}

class DIInjector extends AbstractModule {
  private val logger = LoggerFactory.getLogger(classOf[DIInjector])
  
  override def configure(): Unit = {
    logger.info("binding services")
    bind(classOf[CoinBaseApiService]).to(classOf[HttpBasedCoinBaseApiService])
  }
}
