package components

import java.util.UUID

import domain.models._
import domain.repositories.{OAuthTokenRepository, UserRepository}
import org.joda.time.DateTime

import scala.concurrent.Future

trait ExternalAccountAuthenticator {

  import scala.concurrent.ExecutionContext.Implicits.global

  val externalAccountType: ExternalAccountType.Value

  val oAuthClient: OAuthClient
  val userRepository: UserRepository
  val oauthTokenRepository: OAuthTokenRepository

  def authenticate(code: String, redirectUri: String): Future[OAuthToken] = {
    for {
      externalUserInfo  <- oAuthClient.authorize(code, redirectUri)
      user              <- findUser(externalUserInfo)
      connectedUser     <- connect(user, externalUserInfo)
      token             <- createOAuthToken(connectedUser)
    } yield token
  }

  private def findUser(externalUserInfo: ExternalUserInfo): Future[User] = {
    userRepository.findByExternalAccount(externalAccountType, externalUserInfo.id) map (_.get)
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

  private def connect(user: User, externalUserInfo: ExternalUserInfo): Future[User] = {
    val externalAccount = ExternalAccount(externalAccountType, externalUserInfo.id, "asd")
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
    val userRepository: UserRepository,
    val oauthTokenRepository: OAuthTokenRepository) extends ExternalAccountAuthenticator

object FacebookAuthenticator {
  def apply(
      oAuthClient: FacebookOAuthClient,
      userRepository: UserRepository,
      oauthTokenRepository: OAuthTokenRepository): ExternalAccountAuthenticator = {
    new ExternalAccountAuthenticatorImpl(ExternalAccountType.Facebook, oAuthClient, userRepository, oauthTokenRepository)
  }
}

object GoogleAuthenticator {
  def apply(
     oAuthClient: GoogleOAuthClient,
     userRepository: UserRepository,
     oauthTokenRepository: OAuthTokenRepository): ExternalAccountAuthenticator = {
    new ExternalAccountAuthenticatorImpl(ExternalAccountType.Google, oAuthClient, userRepository, oauthTokenRepository)
  }
}

object LiveAuthenticator {
  def apply(
      oAuthClient: LiveOAuthClient,
      userRepository: UserRepository,
      oauthTokenRepository: OAuthTokenRepository): ExternalAccountAuthenticator = {
    new ExternalAccountAuthenticatorImpl(ExternalAccountType.Live, oAuthClient, userRepository, oauthTokenRepository)
  }
}