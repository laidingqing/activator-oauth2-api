package components

import akka.actor.ActorSystem
import domain._
import domain.models.{LiveUserInfoRepresentation, ExternalUserInfo}
import spray.http.HttpEntity
import spray.httpx.Json4sSupport
import spray.util.LoggingContext

class LiveOAuthClientImpl(val clientId: String, val clientSecret: String)(implicit val actorSystem: ActorSystem, val log: LoggingContext) extends LiveOAuthClient

object LiveOAuthClient {
  def apply(clientId: String, clientSecret: String)(implicit actorSystem: ActorSystem, log: LoggingContext): LiveOAuthClient = {
    new LiveOAuthClientImpl(clientId, clientSecret)(actorSystem: ActorSystem, log: LoggingContext)
  }

}

trait LiveOAuthClientEndpoints extends OAuthEndpoints {
  val AccessTokenEndpoint = "https://login.live.com/oauth20_token.srf"
  val UserAccountEndpoint = "https://apis.live.net/v5.0/me"
}

trait LiveOAuthClient extends SprayOAuthClient with LiveOAuthClientEndpoints with Json4sSupport {
  override def unmarshallExternalUserInfoRepresentationResponse(payload: HttpEntity): ExternalUserInfo = {
    val luir = unmarshall[LiveUserInfoRepresentation](payload)
    LiveUserInfoRepresentation.toExternalUserInfo(luir)
  }
}