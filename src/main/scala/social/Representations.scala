package social

case class AccessTokenRepresentation(access_token: String, token_type: String, expires_in: Option[Long], refresh_token: Option[String])

trait AccountInfoRepresentation

case class FacebookAccountInfoRepresentation(id: String, name: Option[String], first_name: Option[String], last_name: Option[String], email: String) extends AccountInfoRepresentation
case class LiveAccountInfoRepresentation(id: String, name: Option[String], first_name: Option[String], last_name: Option[String], emails: LiveEmailsRepresentation) extends AccountInfoRepresentation
case class LiveEmailsRepresentation(account: String)
case class GoogleAccountInfoRepresentation(id: String, name: Option[String], given_name: Option[String], family_name: Option[String], email: String, verified_email: Boolean) extends AccountInfoRepresentation {
  // Email must have been verified, if not we reject the authorization.
  require(verified_email, s"Unverified email: $email")
}