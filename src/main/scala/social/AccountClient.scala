package social

import akka.actor.ActorSystem
import org.json4s.DefaultFormats
import spray.client.pipelining._
import spray.http.HttpHeaders.Accept
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.Json4sSupport
import spray.httpx.unmarshalling.FromResponseUnmarshaller
import spray.util.LoggingContext

import scala.concurrent.Future

trait AccountClient extends Json4sSupport {

  implicit val actorSystem: ActorSystem
  implicit val log: LoggingContext
  implicit val json4sFormats = DefaultFormats

  protected implicit val _ = actorSystem.dispatcher

  val AccountUri: String

  def getAccountInfo(accessToken: String): Future[AccountInfoRepresentation]
}

class FacebookAccountClient()(implicit val actorSystem: ActorSystem, val log: LoggingContext) extends AccountClient {
  val AccountUri = "https://graph.facebook.com/me"

  def getAccountInfo(accessToken: String): Future[AccountInfoRepresentation] = {
    val request = Get(Uri(AccountUri))

    val pipeline =
      addHeader(Accept(`application/json`)) ~> logRequest(log) ~> sendReceive ~> logResponse(log) ~> unmarshal[FacebookAccountInfoRepresentation]

    pipeline (addCredentials(OAuth2BearerToken(accessToken)) (request))
  }

}

class GoogleAccountClient()(implicit val actorSystem: ActorSystem, val log: LoggingContext) extends AccountClient {
  val AccountUri = "https://www.googleapis.com/oauth2/v1/userinfo"

  def getAccountInfo(accessToken: String): Future[AccountInfoRepresentation] = {
    val request = Get(Uri(AccountUri))

    val pipeline =
      addHeader(Accept(`application/json`)) ~> logRequest(log) ~> sendReceive ~> logResponse(log) ~> unmarshal[GoogleAccountInfoRepresentation]

    pipeline (addCredentials(OAuth2BearerToken(accessToken)) (request))
  }
}

class LiveAccountClient()(implicit val actorSystem: ActorSystem, val log: LoggingContext) extends AccountClient {
  val AccountUri = "https://apis.live.net/v5.0/me"

  def getAccountInfo(accessToken: String): Future[AccountInfoRepresentation] = {
    val request = Get(Uri(AccountUri))

    val pipeline =
      addHeader(Accept(`application/json`)) ~> logRequest(log) ~> sendReceive ~> logResponse(log) ~> unmarshal[LiveAccountInfoRepresentation]

    pipeline (addCredentials(OAuth2BearerToken(accessToken)) (request))
  }
}
