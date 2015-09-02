package social

import akka.actor.ActorSystem
import org.json4s.DefaultFormats
import spray.client.pipelining._
import spray.http.Uri.Query
import spray.http._
import spray.httpx.{UnsuccessfulResponseException, Json4sSupport}
import spray.httpx.unmarshalling._
import spray.util.LoggingContext

import scala.concurrent.Future

trait OAuthClient extends Json4sSupport {

  implicit val actorSystem: ActorSystem
  implicit val log: LoggingContext
  implicit val json4sFormats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all

  protected implicit val _ = actorSystem.dispatcher

  val AccessTokenUri: String

  val clientId: String
  val clientSecret: String

  def authorize(code: String, redirectUri: String): Future[AccessTokenRepresentation] = {

    import spray.httpx.marshalling.BasicMarshallers.FormDataMarshaller
    val params = FormData(Map(
      "client_id"     -> clientId,
      "client_secret" -> clientSecret,
      "code"          -> code,
      "redirect_uri"  -> redirectUri,
      "grant_type"    -> "authorization_code"))

    val request = Post(Uri(AccessTokenUri), params)

    val pipeline = logRequest(log) ~> sendReceive ~> logResponse(log) ~> unmarshal[AccessTokenRepresentation]

    pipeline (request)
  }

}

class FacebookOAuthClient(val clientId: String, val clientSecret: String)(implicit val actorSystem: ActorSystem, val log: LoggingContext) extends OAuthClient {
  val AccessTokenUri = "https://graph.facebook.com/oauth/access_token"

  override def authorize(code: String, redirectUri: String): Future[AccessTokenRepresentation] = {

    import spray.httpx.marshalling.BasicMarshallers.FormDataMarshaller
    val params = FormData(Map(
      "client_id"     -> clientId,
      "client_secret" -> clientSecret,
      "code"          -> code,
      "redirect_uri"  -> redirectUri,
      "grant_type"    -> "authorization_code"))

    val request = Post(Uri(AccessTokenUri), params)

    val pipeline = logRequest(log) ~> sendReceive ~> logResponse(log)

    val response: Future[HttpResponse] = pipeline (request)

    response.flatMap {
      case HttpResponse(StatusCodes.OK, entity, _, _) => Future {
        val accessToken = Query(entity.asString).get("access_token")
        AccessTokenRepresentation(accessToken.get, "Bearer", None, None)
      }
      case response => Future.failed(new UnsuccessfulResponseException(response))
    }
  }

}

class GoogleOAuthClient(val clientId: String, val clientSecret: String)(implicit val actorSystem: ActorSystem, val log: LoggingContext) extends OAuthClient {
  val AccessTokenUri = "https://accounts.google.com/o/oauth2/token"
}

class LiveOAuthClient(val clientId: String, val clientSecret: String)(implicit val actorSystem: ActorSystem, val log: LoggingContext) extends OAuthClient {
  val AccessTokenUri = "https://login.live.com/oauth20_token.srf"
}

