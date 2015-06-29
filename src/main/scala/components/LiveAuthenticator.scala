package components

import akka.actor.ActorSystem
import domain._
import spray.http.HttpEntity
import spray.httpx.Json4sSupport
import spray.util.LoggingContext

class LiveAuthenticatorImpl(val clientId: String, val clientSecret: String)(implicit val actorSystem: ActorSystem, val log: LoggingContext) extends SprayLiveAuthenticator

object LiveAuthenticator {
  def apply(clientId: String, clientSecret: String)(implicit actorSystem: ActorSystem, log: LoggingContext): ExternalProviderAuthenticator = {
    new LiveAuthenticatorImpl(clientId, clientSecret)(actorSystem: ActorSystem, log: LoggingContext)
  }

}

trait LiveEndpoints extends ExternalProviderEndpoints {
  val AccessTokenEndpoint = "https://login.live.com/oauth20_token.srf"
  val UserAccountEndpoint = "https://apis.live.net/v5.0/me"
}

trait SprayLiveAuthenticator extends SprayAuthenticatorClient with LiveEndpoints with Json4sSupport {
  override def unmarshallExternalUserInfoRepresentationResponse(payload: HttpEntity): ExternalUserInfoRepresentation = {
    val luir = unmarshall[LiveUserInfoRepresentation](payload)
    LiveUserInfoRepresentation.toExternalUserInfoRepresentation(luir)
  }
}