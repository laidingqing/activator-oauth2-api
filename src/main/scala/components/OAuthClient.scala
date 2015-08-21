package components

import akka.actor.ActorSystem
import domain._
import domain.models.{ExternalUserInfo, AccessTokenRepresentation}
import org.json4s.native.JsonMethods._
import org.json4s.{DefaultFormats, StringInput}
import spray.client.pipelining._
import spray.http.HttpHeaders.Accept
import spray.http.MediaTypes._
import spray.http._
import spray.httpx.Json4sSupport
import spray.util.LoggingContext

import scala.concurrent.Future

trait OAuthEndpoints {
  val AccessTokenEndpoint: String
  val UserAccountEndpoint: String
}

trait OAuthClient {
    self: OAuthEndpoints =>

  val clientId: String
  val clientSecret: String

  def authorize(code: String, redirectUri: String): Future[ExternalUserInfo]

}

trait SprayOAuthClient extends OAuthClient with Json4sSupport {
    self: OAuthEndpoints =>

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val actorSystem: ActorSystem
  implicit val log: LoggingContext
  implicit val json4sFormats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all

  private lazy val accessTokenUri = Uri(AccessTokenEndpoint)
  private lazy val userAccountUri = Uri(UserAccountEndpoint)

  def authorize(code: String, redirectUri: String): Future[ExternalUserInfo] = {
    for {
      accessToken       <- getAccessToken(code, redirectUri)
      externalUserInfo  <- getExternalUserInfo(accessToken)
    } yield(externalUserInfo)
  }

  private def getAccessToken(code: String, redirectUri: String): Future[AccessTokenRepresentation] = {

    import spray.httpx.marshalling.BasicMarshallers.FormDataMarshaller
    val params = FormData(Map(
      "client_id"     -> clientId,
      "client_secret" -> clientSecret,
      "code"          -> code,
      "redirect_uri"  -> redirectUri,
      "grant_type"    -> "authorization_code"))

    val request = Post(accessTokenUri, params)

    val pipeline = logRequest(log) ~> sendReceive ~> logResponse(log)

    val response: Future[HttpResponse] = pipeline (request)

    response.map {
      case HttpResponse(StatusCodes.OK, entity, _, _) => unmarshallAccessTokenRepresentationResponse(entity)
      case _ => throw RetrieveExternalAccessTokenException(code)
    } recover { case _ => throw RetrieveExternalAccessTokenException(code)}
  }

  protected def unmarshallAccessTokenRepresentationResponse(payload: HttpEntity): AccessTokenRepresentation = {
    unmarshall[AccessTokenRepresentation](payload)
  }

  private def getExternalUserInfo(accessToken: AccessTokenRepresentation): Future[ExternalUserInfo] = {
    val request = Get(userAccountUri)

    val pipeline =
      addHeader(Accept(`application/json`)) ~> logRequest(log) ~> sendReceive ~> logResponse(log)

    val response: Future[HttpResponse] = pipeline (addCredentials(OAuth2BearerToken(accessToken.access_token)) (request))
    response.map {
      case HttpResponse(StatusCodes.OK, entity, _, _) => unmarshallExternalUserInfoRepresentationResponse(entity)
      case _ => throw RetrieveExternalUserInfoException(accessToken.access_token)
    } recover { case _ => throw RetrieveExternalUserInfoException(accessToken.access_token)}

  }

  protected def unmarshallExternalUserInfoRepresentationResponse(payload: HttpEntity): ExternalUserInfo = {
    unmarshall[ExternalUserInfo](payload)
  }

  protected def unmarshall[T <: AnyRef](payload: HttpEntity)(implicit mf: scala.reflect.Manifest[T]): T = {
    parse(StringInput(payload.data.asString(HttpCharsets.`UTF-8`))).extract[T]
  }

}

