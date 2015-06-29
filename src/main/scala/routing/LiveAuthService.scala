package routing

import akka.actor.ActorSystem
import components._
import org.json4s.DefaultFormats
import spray.httpx.Json4sSupport
import spray.routing._

import scala.concurrent.ExecutionContext

class LiveAuthServiceImpl(val liveAuthenticator: ExternalProviderAuthenticator)(implicit val actorSystem: ActorSystem) extends LiveAuthService

object LiveAuthService {
  def apply(liveAuthenticator: ExternalProviderAuthenticator)(implicit actorSystem: ActorSystem): LiveAuthService = {
    new LiveAuthServiceImpl(liveAuthenticator)(actorSystem)
  }
}

trait LiveAuthService extends Directives with Json4sSupport {

  implicit def json4sFormats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
  implicit val executionContext: ExecutionContext = ExecutionContext.global

  val liveAuthenticator: ExternalProviderAuthenticator

  val AccesTokenPath = "access-token"

  def route =
    path(AccesTokenPath ) {
      post {
        parameters('grant_type ! "live_auth_code", 'code, 'redirect_uri) { (code, redirect_uri) => {
          onSuccess(liveAuthenticator.authenticate(code, redirect_uri)) { externalUserInfo =>
            complete(externalUserInfo)
          }
        }
        }
      }
    }

}