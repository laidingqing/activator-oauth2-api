package components

import akka.actor.ActorSystem
import domain.models.{AccessTokenRepresentation, ExternalUserInfo, FacebookUserInfoRepresentation}
import spray.http.Uri.Query
import spray.http._
import spray.util.LoggingContext

class FacebookOAuthClientImpl(val clientId: String, val clientSecret: String)(implicit val actorSystem: ActorSystem, val log: LoggingContext) extends FacebookOAuthClient

object FacebookOAuthClient {
  def apply(clientId: String, clientSecret: String)(implicit actorSystem: ActorSystem, log: LoggingContext): FacebookOAuthClient = {
    new FacebookOAuthClientImpl(clientId, clientSecret)(actorSystem: ActorSystem, log: LoggingContext)
  }
}

trait FacebookOAuthEndpoints extends OAuthEndpoints {
  val AccessTokenEndpoint = "https://graph.facebook.com/oauth/access_token"
  val UserAccountEndpoint = "https://graph.facebook.com/me"
}

trait FacebookOAuthClient extends SprayOAuthClient with FacebookOAuthEndpoints {
  override def unmarshallAccessTokenRepresentationResponse(payload: HttpEntity): AccessTokenRepresentation = {
    val accessToken = Query(payload.asString).get("access_token")
    AccessTokenRepresentation(accessToken.get, "Bearer", None, None)
  }

  override def unmarshallExternalUserInfoRepresentationResponse(payload: HttpEntity): ExternalUserInfo = {
    val fuir = unmarshall[FacebookUserInfoRepresentation](payload)
    FacebookUserInfoRepresentation.toExternalUserInfo(fuir)
  }
}
