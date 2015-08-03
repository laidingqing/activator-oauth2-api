package routing

import akka.actor.ActorSystem
import spray.routing.Directives

class StaticDeliveryServiceImpl(val name: String)(implicit val actorSystem: ActorSystem) extends StaticDeliveryService

object StaticDeliveryService {
  def apply(name: String)(implicit actorSystem: ActorSystem): StaticDeliveryService = new StaticDeliveryServiceImpl(name)(actorSystem)
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
