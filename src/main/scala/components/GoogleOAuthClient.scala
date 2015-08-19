package components

import akka.actor.ActorSystem
import domain._
import domain.models.{GoogleUserInfoRepresentation, ExternalUserInfo}
import spray.http.HttpEntity
import spray.util.LoggingContext

class GoogleOAuthClientImpl(val clientId: String, val clientSecret: String)
    (implicit val actorSystem: ActorSystem, val log: LoggingContext) extends GoogleOAuthClient

object GoogleOAuthClient {
  def apply(clientId: String, clientSecret: String)(implicit actorSystem: ActorSystem, log: LoggingContext): GoogleOAuthClient = {
    new GoogleOAuthClientImpl(clientId, clientSecret)(actorSystem: ActorSystem, log: LoggingContext)
  }
}

trait GoogleOAuthClientEndpoints extends OAuthEndpoints {
  val AccessTokenEndpoint = "https://accounts.google.com/o/oauth2/token"
  val UserAccountEndpoint = "https://www.googleapis.com/oauth2/v1/userinfo"
}

trait GoogleOAuthClient extends SprayOAuthClient with GoogleOAuthClientEndpoints {
  override def unmarshallExternalUserInfoRepresentationResponse(payload: HttpEntity): ExternalUserInfo = {
    val guir = unmarshall[GoogleUserInfoRepresentation](payload)
    GoogleUserInfoRepresentation.toExternalUserInfo(guir)
  }
}