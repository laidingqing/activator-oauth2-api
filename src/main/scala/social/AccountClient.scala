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

trait AccountClient[T <: AccountInfoRepresentation] extends Json4sSupport {

  implicit val actorSystem: ActorSystem
  implicit val log: LoggingContext
  implicit val json4sFormats = DefaultFormats

  private implicit val _ = actorSystem.dispatcher

  val AccountUri: String

  def getAccountInfo[T: FromResponseUnmarshaller](accessToken: String): Future[T] = {
    val request = Get(Uri(AccountUri))

    val pipeline =
      addHeader(Accept(`application/json`)) ~> logRequest(log) ~> sendReceive ~> logResponse(log) ~> unmarshal[T]

    pipeline (addCredentials(OAuth2BearerToken(accessToken)) (request))
  }
}

class FacebookAccountClient()(implicit val actorSystem: ActorSystem, val log: LoggingContext) extends AccountClient[FacebookAccountInfoRepresentation] {
  val AccountUri = "https://graph.facebook.com/me"
}

class GoogleAccountClient()(implicit val actorSystem: ActorSystem, val log: LoggingContext) extends AccountClient[GoogleAccountInfoRepresentation] {
  val AccountUri = "https://www.googleapis.com/oauth2/v1/userinfo"
}

class LiveAccountClient()(implicit val actorSystem: ActorSystem, val log: LoggingContext) extends AccountClient[LiveAccountInfoRepresentation] {
  val AccountUri = "https://apis.live.net/v5.0/me"
}
