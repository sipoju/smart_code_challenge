package smart

import akka.http.scaladsl.model.DateTime

case class PriceCalculationRequest(id: Double, underlying: Double, volatility: Double)

case class PriceCalculationResponse(price: Double, date: DateTime)