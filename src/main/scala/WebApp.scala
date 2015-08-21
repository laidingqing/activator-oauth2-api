import akka.io.IO
import core._
import spray.can.Http

object WebApp extends App with BootedCore with SettingsCore with PersistenceCore with OAuthCore with AuthorizationCore with ApiCore {
  IO(Http) ! Http.Bind(routedHttpServiceActor, settings.api.host, port = settings.api.port)
}
