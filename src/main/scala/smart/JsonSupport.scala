package smart

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.DateTime
import com.typesafe.scalalogging.StrictLogging
import spray.json._

trait JsonSupport extends SprayJsonSupport with StrictLogging {

  import DefaultJsonProtocol._

  implicit object DateTimeJsonFormat extends JsonFormat[DateTime] {
    def write(x: DateTime) = JsString(x.toIsoDateTimeString())
    def read(value: JsValue) = value match {
      case JsString(date) =>
        DateTime.fromIsoDateTimeString(date) match {
          case None => deserializationError("Expected DateTime as IsoDateTimeString, but got " + date)
          case Some(dt) => dt
        }
      case x => deserializationError("Expected DateTime as IsoDateTimeString, but got " + x)
    }
  }

  implicit val implicitPriceCalculationResponse = jsonFormat2(PriceCalculationResponse)
  implicit val implicitPriceCalculationParams = jsonFormat3(PriceCalculationRequest)

}