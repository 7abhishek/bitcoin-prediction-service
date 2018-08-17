package serializers

import models.{BitCoinInstantPrice, BitCoinTradingDecision}
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsError, Json, JsResult, JsString, JsSuccess, JsValue, Reads, Writes}

object JsonSerializers {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ")
  
  private implicit val jodaTimeInstantReads = new Reads[Instant] {
    override def reads(json: JsValue): JsResult[Instant] = {
      val jsonString = json.asInstanceOf[JsString]
      Instant.parse(jsonString.value, dateTimeFormatter) match {
        case instant: Instant => JsSuccess(instant)
        case error => JsError(s"could not parse $json to joda instant. Error: $error")
      }
    }
  }

  private implicit val jodaTimeInstantWrites = new Writes[Instant] {
    override def writes(instant: Instant): JsValue = JsString(instant.toString)
  }
  
  implicit val bitCoinInstantPriceFormat = Json.format[BitCoinInstantPrice]
  
  implicit val bitCoinTradingDecisionReads = Reads.enumNameReads(BitCoinTradingDecision)
  implicit val bitCoinTradingDecisionWrites = Writes.enumNameWrites
}
