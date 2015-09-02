package components

import java.util.UUID

import domain.models._
import domain.repositories.{OAuthTokenRepository, UserRepository}
import org.joda.time.DateTime
import social._

import scala.concurrent.Future

trait ExternalAccountAuthenticator {

  import scala.concurrent.ExecutionContext.Implicits.global

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

  private def getUser(externalUserInfo: ExternalUserInfo): Future[User] = {
    userRepository.findByExternalAccount(externalAccountType, externalUserInfo.id) flatMap {
      case Some(user) => Future(user)
      case _ => createUser(externalUserInfo)
    }
  }

  private def createUser(externalUserInfo: ExternalUserInfo): Future[User] = {
    userRepository.create(
      User(
        UUID.randomUUID(),
        externalUserInfo.firstname,
        externalUserInfo.lastname,
        externalUserInfo.email,
        Seq.empty
      ))
  }

  private def connect(user: User, externalUserInfo: ExternalUserInfo, accessToken: AccessTokenRepresentation): Future[User] = {
    val externalAccount = ExternalAccount(externalAccountType, externalUserInfo.id, accessToken.access_token)
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
    val oauthTokenRepository: OAuthTokenRepository) extends ExternalAccountAuthenticator

object FacebookAuthenticator {
  def apply(
      oAuthClient: FacebookOAuthClient,
      externalAccountClient: FacebookAccountClient,
      userRepository: UserRepository,
      oauthTokenRepository: OAuthTokenRepository): ExternalAccountAuthenticator = {
    new ExternalAccountAuthenticatorImpl(ExternalAccountType.Facebook, oAuthClient, externalAccountClient, userRepository, oauthTokenRepository)
  }
}

object GoogleAuthenticator {
  def apply(
     oAuthClient: GoogleOAuthClient,
     externalAccountClient: GoogleAccountClient,
     userRepository: UserRepository,
     oauthTokenRepository: OAuthTokenRepository): ExternalAccountAuthenticator = {
    new ExternalAccountAuthenticatorImpl(ExternalAccountType.Google, oAuthClient, externalAccountClient, userRepository, oauthTokenRepository)
  }
}

object LiveAuthenticator {
  def apply(
      oAuthClient: LiveOAuthClient,
      externalAccountClient: LiveAccountClient,
      userRepository: UserRepository,
      oauthTokenRepository: OAuthTokenRepository): ExternalAccountAuthenticator = {
    new ExternalAccountAuthenticatorImpl(ExternalAccountType.Live, oAuthClient, externalAccountClient, userRepository, oauthTokenRepository)
  }
}