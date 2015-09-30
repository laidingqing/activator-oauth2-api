package routing

import akka.actor.ActorSystem
import domain.repositories.UserRepository
import org.json4s.DefaultFormats
import spray.httpx.Json4sSupport
import spray.routing._
import scala.concurrent.ExecutionContext
import java.util.UUID
import org.json4s.ext.JavaTypesSerializers

class UserServicesImpl(val userRepository: UserRepository)(implicit val actorSystem: ActorSystem) extends UserServices

object UserServices {
  def apply(userRepository: UserRepository)(implicit actorSystem: ActorSystem): UserServices = {
    new UserServicesImpl(userRepository)(actorSystem)
  }
}

trait UserServices extends Directives with Json4sSupport {

  implicit def json4sFormats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all ++ JavaTypesSerializers.all
  implicit val executionContext: ExecutionContext = ExecutionContext.global

  def userRepository: UserRepository

  def route = pathPrefix("users" / "me") {
    pathEnd {
      get {
       complete { userRepository.retrieve(UUID.fromString("1")) }
      }
    }
  }
}
