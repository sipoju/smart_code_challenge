package smart

import java.util.{Timer, TimerTask}

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.DateTime
import akka.http.scaladsl.model.StatusCodes.{OK, RequestTimeout, TooManyRequests}
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post, _}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Random

class PriceService(configMainRequestsLimit: Int, requestTimeout: Int, configClientsRequestsLimit: List[(String, Int)])(implicit ec: ExecutionContext, system: ActorSystem) extends JsonSupport {

  var requestsLimit = collection.mutable.Map[String, Int]()

  def clearRequestsLimit(): Unit = {
    requestsLimit = collection.mutable.Map(configClientsRequestsLimit.toSeq: _*)
  }

  def calculatePrice(clientId: String, entity: PriceCalculationRequest): Future[Either[String, PriceCalculationResponse]] = {
    val promise = Promise[Either[String, PriceCalculationResponse]]

    val mainCalculation = Future {

      Right(PriceCalculationResponse(price = Random.nextDouble(), date = DateTime.now))

    }

    promise.tryCompleteWith(mainCalculation)

    val timerTask = new TimerTask {
      override def run(): Unit = promise.completeWith(Future.successful(Left("Unable to serve response within time limit, please enhance your calm.")))
    }
    val timer = new Timer()
    timer.schedule(timerTask, requestTimeout * 1000)

    promise.future
  }


  val route =
    path(Segment / "price") { clientId =>
      post {
        entity(as[PriceCalculationRequest]) { param =>
          complete {
            val limit = requestsLimit.getOrElseUpdate(clientId, configMainRequestsLimit)
            if (limit <= 0) {
              TooManyRequests -> TooManyRequests.defaultMessage
            } else {
              requestsLimit += clientId -> (limit - 1)

              calculatePrice(clientId, param).map[ToResponseMarshallable] {
                case Right(identity) => OK -> identity
                case Left(err) => RequestTimeout -> err
              }
            }
          }
        }
      }
    }
}
