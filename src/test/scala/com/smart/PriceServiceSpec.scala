package com.smart

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.typesafe.scalalogging.StrictLogging
import smart.{JsonSupport, PriceCalculationRequest, PriceCalculationResponse, PriceService}
import org.scalatest.{Matchers, WordSpec}
import spray.json._
import scala.concurrent.duration._

class PriceServiceSpec extends WordSpec with Matchers with ScalatestRouteTest with StrictLogging with JsonSupport {
  {
    implicit def default(implicit system: ActorSystem) = RouteTestTimeout(5 seconds)

    val priceService = new PriceService(1, 2, List())

    "The service" should {
      "return a PriceCalculationResponse data response for post requests to the /abc/price path" in {
        Post("/abc/price").
          withEntity(HttpEntity(ContentTypes.`application/json`, PriceCalculationRequest(1, 2, 3).toJson.toString)) ~> priceService.route ~> check {
          status shouldEqual StatusCodes.OK

          entityAs[PriceCalculationResponse]

        }
        priceService.clearRequestsLimit()
      }

      "return TooManyRequests StatusCode for second post requests to the /abc/price path" in {
        Post("/abc/price").
          withEntity(HttpEntity(ContentTypes.`application/json`, PriceCalculationRequest(1, 2, 3).toJson.toString)) ~> priceService.route ~> check {
          status shouldEqual StatusCodes.OK
          entityAs[PriceCalculationResponse]
        }

        Post("/abc/price").
          withEntity(HttpEntity(ContentTypes.`application/json`, PriceCalculationRequest(1, 2, 3).toJson.toString)) ~> priceService.route ~> check {
          status shouldEqual StatusCodes.TooManyRequests
        }

        priceService.clearRequestsLimit()
      }
    }
  }
}
