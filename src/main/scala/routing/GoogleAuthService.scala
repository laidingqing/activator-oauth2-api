package routing

import akka.actor.ActorSystem
import components._
import org.json4s.DefaultFormats
import spray.httpx.Json4sSupport
import spray.routing._

import scala.concurrent.ExecutionContext

class GoogleAuthServiceImpl(val googleAuthenticator: ExternalProviderAuthenticator)(implicit val actorSystem: ActorSystem) extends GoogleAuthService

object GoogleAuthService {
  def apply(googleAuthenticator: ExternalProviderAuthenticator)(implicit actorSystem: ActorSystem): GoogleAuthService = {
    new GoogleAuthServiceImpl(googleAuthenticator)(actorSystem)
  }
}

trait GoogleAuthService extends Directives with Json4sSupport {

  implicit def json4sFormats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
  implicit val executionContext: ExecutionContext = ExecutionContext.global

  val googleAuthenticator: ExternalProviderAuthenticator

  val AccesTokenPath = "access-token"

  def route =
    path(AccesTokenPath ) {
      post {
        parameters('grant_type ! "google_auth_code", 'code, 'redirect_uri) { (code, redirect_uri) => {
          onSuccess(googleAuthenticator.authenticate(code, redirect_uri)) { externalUserInfo =>
              complete(externalUserInfo)
            }
          }
        }
      }
    }

}