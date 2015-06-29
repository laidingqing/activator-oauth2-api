package conf

import com.typesafe.config.{Config, ConfigFactory}

case class ApiSettings(
  host: String,
  port: Int
)

case class ServiceSettings(
  clientId: String,
  clientSecret: String
)

class Settings(config: Config) {

  // API
  val api = ApiSettings(
    config.getString("api.host"),
    config.getInt("api.port")
  )

  // Services
  val facebook = ServiceSettings(
    config.getString("services.facebook.client-id"),
    config.getString("services.facebook.client-secret")
  )

  val google = ServiceSettings(
    config.getString("services.google.client-id"),
    config.getString("services.google.client-secret")
  )

  val live = ServiceSettings(
    config.getString("services.live.client-id"),
    config.getString("services.live.client-secret")
  )

}

object Settings {
  def apply() = {
    new Settings(ConfigFactory.load())
  }

  def apply(config: Config) = {
    new Settings(config)
  }
}


