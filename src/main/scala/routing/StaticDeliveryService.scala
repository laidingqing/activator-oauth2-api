package routing

import akka.actor.ActorSystem
import spray.routing.Directives

class StaticDeliveryServiceImpl()(implicit val actorSystem: ActorSystem) extends StaticDeliveryService

object StaticDeliveryService {
  def apply()(implicit actorSystem: ActorSystem): StaticDeliveryService = new StaticDeliveryServiceImpl()(actorSystem)
}

trait StaticDeliveryService extends Directives {
  implicit val actorSystem: ActorSystem

  def route =
    get {
      pathPrefix("example") {
        pathEndOrSingleSlash {
          getFromResource("public/index.html")
        } ~
          getFromResourceDirectory("public")
      }
    }

}
