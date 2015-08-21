package core

import akka.actor.ActorSystem
import components._
import conf.Settings
import domain.repositories._
import routing._
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

trait OAuthCore {
    self: Core with SettingsCore =>

  val facebookOAuthClient = FacebookOAuthClient(settings.facebook.clientId, settings.facebook.clientSecret)
  val googleOAuthClient = GoogleOAuthClient(settings.google.clientId, settings.google.clientSecret)
  val liveOAuthClient = LiveOAuthClient(settings.live.clientId, settings.live.clientSecret)

}

trait AuthorizationCore {
    self: OAuthCore with PersistenceCore with SettingsCore =>

  val facebookAuthenticator = FacebookAuthenticator(facebookOAuthClient, userRepository, oauthTokenRepository)
  val googleAuthenticator = GoogleAuthenticator(googleOAuthClient, userRepository, oauthTokenRepository)
  val liveAuthenticator = LiveAuthenticator(liveOAuthClient, userRepository, oauthTokenRepository)

}

trait PersistenceCore {
  self: Core =>

  //implicit val repositoryExecutionContext = ExecutionContext.global
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
    StaticDeliveryService().route ~
    ExampleService().route


  val routedHttpServiceActor = actorSystem.actorOf(RoutedHttpService.props(servicesRoutes), "oauth2-api-root-service")

}



