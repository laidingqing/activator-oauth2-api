package components

import akka.actor.ActorSystem
import domain._
import spray.http.HttpEntity
import spray.util.LoggingContext

class GoogleAuthenticatorImpl(val clientId: String, val clientSecret: String)(implicit val actorSystem: ActorSystem, val log: LoggingContext) extends SprayGoogleAuthenticator

object GoogleAuthenticator {
  def apply(clientId: String, clientSecret: String)(implicit actorSystem: ActorSystem, log: LoggingContext): ExternalProviderAuthenticator = {
    new GoogleAuthenticatorImpl(clientId, clientSecret)(actorSystem: ActorSystem, log: LoggingContext)
  }
}

trait GoogleEndpoints extends ExternalProviderEndpoints {
  val AccessTokenEndpoint = "https://accounts.google.com/o/oauth2/token"
  val UserAccountEndpoint = "https://www.googleapis.com/oauth2/v1/userinfo"
}

trait SprayGoogleAuthenticator extends SprayAuthenticatorClient with GoogleEndpoints {
  override def unmarshallExternalUserInfoRepresentationResponse(payload: HttpEntity): ExternalUserInfoRepresentation = {
    val guir = unmarshall[GoogleUserInfoRepresentation](payload)
    GoogleUserInfoRepresentation.toExternalUserInfoRepresentation(guir)
  }
}