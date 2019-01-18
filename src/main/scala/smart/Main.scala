package smart

import java.net.InetAddress

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.io.StdIn

object Main extends App {

  val config = ConfigFactory.load()
  val configMainRequestsLimit = config.getInt("appConfig.requestsLimitMain")
  val configClientsRequestsLimit = config.getConfigList("appConfig.requestsLimitClients").asScala.toList.map(cfg => (cfg.getString("clientName"), cfg.getInt("limit")))
  val requestTimeout = config.getInt("appConfig.requestTimeout")


  implicit val system = ActorSystem("smart-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val priceService = new PriceService(configMainRequestsLimit, requestTimeout, configClientsRequestsLimit)

  system.scheduler.schedule(0 seconds, 1 second){
    priceService.clearRequestsLimit()
  }

  val interface = InetAddress.getLocalHost.getHostAddress
  val bindingFuture = Http().bindAndHandle(priceService.route, interface, 8080)

  println(s"Server online at http://$interface:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}
