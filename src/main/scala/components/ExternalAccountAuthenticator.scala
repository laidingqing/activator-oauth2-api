package components

import java.util.UUID

import akka.actor.ActorSystem
import domain.models._
import domain.repositories.{OAuthTokenRepository, UserRepository}
import org.joda.time.DateTime
import org.json4s.DefaultFormats
import social._
import spray.httpx.{SprayJsonSupport, Json4sSupport}

import scala.concurrent.Future

trait ExternalAccountAuthenticator {

  implicit val actorSystem: ActorSystem
  implicit val _ = actorSystem.dispatcher

  val externalAccountType: ExternalAccountType.Value

  val oAuthClient: OAuthClient
  val externalAccountClient: AccountClient

  val userRepository: UserRepository
  val oauthTokenRepository: OAuthTokenRepository

  def authenticate(code: String, redirectUri: String): Future[OAuthToken] = {
    for {
      accessToken         <- oAuthClient.authorize(code, redirectUri)
      externalAccountInfo <- externalAccountClient.getAccountInfo(accessToken.access_token)
      user                <- getUser(externalAccountInfo)
      connectedUser       <- connect(user, externalAccountInfo, accessToken)
      token               <- createOAuthToken(connectedUser)
    } yield token
  }

  private def getUser(accountInfoRepresentation: AccountInfoRepresentation): Future[User] = {
    userRepository.findByExternalAccount(externalAccountType, accountInfoRepresentation.accountId) flatMap {
      case Some(user) => Future(user)
      case _ => createUser(accountInfoRepresentation)
    }
  }

  private def createUser(accountInfoRepresentation: AccountInfoRepresentation): Future[User] = {
    userRepository.create(
      User(
        UUID.randomUUID(),
        accountInfoRepresentation.accountFirstname,
        accountInfoRepresentation.accountLastname,
        accountInfoRepresentation.accountEmail,
        Seq.empty
      ))
  }

  private def connect(user: User, accountInfoRepresentation: AccountInfoRepresentation, accessToken: AccessTokenRepresentation): Future[User] = {
    val externalAccount =
      ExternalAccount(
        externalAccountType,
        accountInfoRepresentation.accountId,
        accessToken.access_token,
        accessToken.refresh_token)
    userRepository.update(user.copy(externalAccounts = user.externalAccounts :+ externalAccount))
  }

  private def createOAuthToken(user: User): Future[OAuthToken] = {
    oauthTokenRepository.create(
      OAuthToken(
        UUID.randomUUID(),
        UUID.randomUUID().toString,
        DateTime.now(),
        true,
        user.id)
    )
  }
}

class ExternalAccountAuthenticatorImpl(
    val externalAccountType: ExternalAccountType.Value,
    val oAuthClient: OAuthClient,
    val externalAccountClient: AccountClient,
    val userRepository: UserRepository,
    val oauthTokenRepository: OAuthTokenRepository)(implicit val actorSystem: ActorSystem) extends ExternalAccountAuthenticator

object FacebookAuthenticator {
  def apply(
      oAuthClient: FacebookOAuthClient,
      externalAccountClient: FacebookAccountClient,
      userRepository: UserRepository,
      oauthTokenRepository: OAuthTokenRepository)(implicit actorSystem: ActorSystem): ExternalAccountAuthenticator = {
    new ExternalAccountAuthenticatorImpl(ExternalAccountType.Facebook, oAuthClient, externalAccountClient, userRepository, oauthTokenRepository)
  }
}

object GoogleAuthenticator {
  def apply(
     oAuthClient: GoogleOAuthClient,
     externalAccountClient: GoogleAccountClient,
     userRepository: UserRepository,
     oauthTokenRepository: OAuthTokenRepository)(implicit actorSystem: ActorSystem): ExternalAccountAuthenticator = {
    new ExternalAccountAuthenticatorImpl(ExternalAccountType.Google, oAuthClient, externalAccountClient, userRepository, oauthTokenRepository)
  }
}

object LiveAuthenticator {
  def apply(
      oAuthClient: LiveOAuthClient,
      externalAccountClient: LiveAccountClient,
      userRepository: UserRepository,
      oauthTokenRepository: OAuthTokenRepository)(implicit actorSystem: ActorSystem): ExternalAccountAuthenticator = {
    new ExternalAccountAuthenticatorImpl(ExternalAccountType.Live, oAuthClient, externalAccountClient, userRepository, oauthTokenRepository)
  }
}