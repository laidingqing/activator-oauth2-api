package components

import akka.actor.ActorSystem
import domain.AccessTokenRepresentation
import spray.http.Uri.Query
import spray.http._
import spray.util.LoggingContext

class FacebookAuthenticatorImpl(val clientId: String, val clientSecret: String)(implicit val actorSystem: ActorSystem, val log: LoggingContext) extends SprayFacebookAuthenticator

object FacebookAuthenticator {
  def apply(clientId: String, clientSecret: String)(implicit actorSystem: ActorSystem, log: LoggingContext): ExternalProviderAuthenticator = {
    new FacebookAuthenticatorImpl(clientId, clientSecret)(actorSystem: ActorSystem, log: LoggingContext)
  }
}

trait FacebookEndpoints extends ExternalProviderEndpoints {
  val AccessTokenEndpoint = "https://graph.facebook.com/oauth/access_token"
  val UserAccountEndpoint = "https://graph.facebook.com/me"
}

trait SprayFacebookAuthenticator extends SprayAuthenticatorClient with FacebookEndpoints {
  override def unmarshallAccessTokenRepresentationResponse(payload: HttpEntity): AccessTokenRepresentation = {
    val accessToken = Query(payload.asString).get("access_token")
    AccessTokenRepresentation(accessToken.get, "Bearer", None, None)
  }
}
