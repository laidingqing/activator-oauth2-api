package routing

import akka.actor.ActorSystem
import components.ExternalProviderAuthenticator
import org.json4s.DefaultFormats
import spray.httpx.Json4sSupport
import spray.routing._

import scala.concurrent.ExecutionContext

class FacebookAuthServiceImpl(val fbAuthenticator: ExternalProviderAuthenticator)(implicit val actorSystem: ActorSystem) extends FacebookAuthService

object FacebookAuthService {
  def apply(fbAuthenticator: ExternalProviderAuthenticator)(implicit actorSystem: ActorSystem): FacebookAuthService = {
    new FacebookAuthServiceImpl(fbAuthenticator)(actorSystem)
  }
}

trait FacebookAuthService extends Directives with Json4sSupport {

  implicit def json4sFormats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all
  implicit val executionContext: ExecutionContext = ExecutionContext.global

  val fbAuthenticator: ExternalProviderAuthenticator

  val AccesTokenPath = "access-token"

  def route =
    path(AccesTokenPath ) {
      post {
        parameters('grant_type ! "facebook_auth_code", 'code, 'redirect_uri) { (code, redirect_uri) => {
            onSuccess(fbAuthenticator.authenticate(code, redirect_uri)) { externalUserInfo =>
              complete(externalUserInfo)
            }
          }
        }
      }
    }

}