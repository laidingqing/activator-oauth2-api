package core

import akka.actor.ActorSystem
import components.{FacebookAuthenticator, GoogleAuthenticator, LiveAuthenticator}
import conf.Settings
import domain.repositories._
import routing._
import spray.routing.RouteConcatenation

import scala.concurrent.ExecutionContext

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

trait ComponentsCore {
  self: Core with SettingsCore =>

  val facebookAuthenticator = FacebookAuthenticator(settings.facebook.clientId, settings.facebook.clientSecret)
  val googleAuthenticator = GoogleAuthenticator(settings.google.clientId, settings.google.clientSecret)
  val liveAuthenticator = LiveAuthenticator(settings.live.clientId, settings.live.clientSecret)

}

trait RepositoriesCore {

  implicit val repositoryExecutionContext = ExecutionContext.global

  val userRepository: UserRepository = new UserRepositoryImpl()
  val todoItemRepository: TodoItemRepository = new TodoItemRepositoryImpl()
}

trait ApiCore extends RouteConcatenation {
  self: Core with ComponentsCore with SettingsCore with RepositoriesCore =>

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



