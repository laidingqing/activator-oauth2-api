package core

import akka.actor.ActorSystem
import components._
import conf.Settings
import domain.repositories._
import routing._
import social._
import spray.routing.RouteConcatenation

trait Core {
  implicit def actorSystem: ActorSystem
}

trait BootedCore extends Core {
  implicit lazy val actorSystem = ActorSystem("oauth2-api")

  actorSystem.registerOnTermination {
    actorSystem.log.info("OAuth2 API shutdown.")
  }
}

trait SettingsCore {
  val settings = Settings()
}

trait SocialCore {
    self: Core with SettingsCore =>

  val facebookOAuthClient = new FacebookOAuthClient(settings.facebook.clientId, settings.facebook.clientSecret)
  val googleOAuthClient = new GoogleOAuthClient(settings.google.clientId, settings.google.clientSecret)
  val liveOAuthClient = new LiveOAuthClient(settings.live.clientId, settings.live.clientSecret)

  val facebookAccountClient = new FacebookAccountClient()
  val googleAccountClient = new GoogleAccountClient()
  val liveAccountClient = new LiveAccountClient()

}

trait AuthorizationCore {
    self: Core with SocialCore with PersistenceCore with SettingsCore =>

  private implicit val _ = actorSystem.dispatcher

  val facebookAuthenticator = FacebookAuthenticator(facebookOAuthClient, facebookAccountClient, userRepository, oauthTokenRepository)
  val googleAuthenticator = GoogleAuthenticator(googleOAuthClient, googleAccountClient, userRepository, oauthTokenRepository)
  val liveAuthenticator = LiveAuthenticator(liveOAuthClient, liveAccountClient, userRepository, oauthTokenRepository)

}

trait PersistenceCore {
  self: Core =>

  private implicit val _ = actorSystem.dispatcher

  val userRepository = UserRepository()
  val oauthTokenRepository = OAuthTokenRepository()
  val todoItemRepository = TodoItemRepository()
}

trait ApiCore extends RouteConcatenation {
    self: Core with AuthorizationCore with PersistenceCore =>

  private implicit val _ = actorSystem.dispatcher

  val servicesRoutes =
    FacebookAuthService(facebookAuthenticator).route ~
    GoogleAuthService(googleAuthenticator).route ~
    LiveAuthService(liveAuthenticator).route ~
    UserServices(userRepository).route ~
    StaticDeliveryService().route //~
    ExampleService().route


  val routedHttpServiceActor = actorSystem.actorOf(RoutedHttpService.props(servicesRoutes), "oauth2-api-root-service")

}



