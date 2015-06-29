package core

import akka.actor.ActorSystem
import components.{FacebookAuthenticator, GoogleAuthenticator, LiveAuthenticator}
import conf.Settings
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


trait ComponentsCore {
  self: Core with SettingsCore =>

  val facebookAuthenticator = FacebookAuthenticator(settings.facebook.clientId, settings.facebook.clientSecret)
  val googleAuthenticator = GoogleAuthenticator(settings.google.clientId, settings.google.clientSecret)
  val liveAuthenticator = LiveAuthenticator(settings.live.clientId, settings.live.clientSecret)

}

trait ApiCore extends RouteConcatenation {
  self: Core with ComponentsCore with SettingsCore =>

  private implicit val _ = actorSystem.dispatcher

  val servicesRoutes =
    FacebookAuthService(facebookAuthenticator).route ~
    GoogleAuthService(googleAuthenticator).route ~
    LiveAuthService(liveAuthenticator).route

  val routedHttpServiceActor = actorSystem.actorOf(RoutedHttpService.props(servicesRoutes), "oauth2-api-root-service")

}



