package domain

trait Representation

case class AccessTokenRepresentation(access_token: String, token_type: String, expires_in: Option[Long], refresh_token: Option[String]) extends Representation
case class ExternalUserInfoRepresentation(id: String, name: Option[String], first_name: Option[String], last_name: Option[String], email: String) extends Representation

case class LiveUserInfoRepresentation(id: String, name: Option[String], first_name: Option[String], last_name: Option[String], emails: LiveEmailsRepresentation) extends Representation
case class LiveEmailsRepresentation(account: String)
object LiveUserInfoRepresentation {
  implicit def toExternalUserInfoRepresentation(liveUserInfoRepresentation: LiveUserInfoRepresentation): ExternalUserInfoRepresentation = {
    ExternalUserInfoRepresentation(
      liveUserInfoRepresentation.id,
      liveUserInfoRepresentation.name,
      liveUserInfoRepresentation.first_name,
      liveUserInfoRepresentation.last_name,
      liveUserInfoRepresentation.emails.account
    )
  }
}

case class GoogleUserInfoRepresentation(id: String, name: Option[String], given_name: Option[String], family_name: Option[String], email: String, verified_email: Boolean) extends Representation {
  // Email must have been verified, if not we reject the authentication.
  require(verified_email, s"Unverified email: $email")
}
object GoogleUserInfoRepresentation {
  implicit def toExternalUserInfoRepresentation(googleUserInfoRepresentation: GoogleUserInfoRepresentation): ExternalUserInfoRepresentation = {
    ExternalUserInfoRepresentation(
      googleUserInfoRepresentation.id,
      googleUserInfoRepresentation.name,
      googleUserInfoRepresentation.given_name,
      googleUserInfoRepresentation.family_name,
      googleUserInfoRepresentation.email
    )
  }
}

case class ErrorMessageRepresentation(code: String, message: String) extends Representation
case class ExternalUserInfoMessageErrorRepresentation(code: String, message: String, externalAccountInfo: ExternalUserInfoRepresentation) extends Representation

object ErrorMessageRepresentation {
  val FailedAccessTokenRetrievalFromExternalService = ErrorMessageRepresentation("40110", "Error while retrieving 'access_token' from external service.")
  val FailedUserInfoRetrievalFromExternalService = ErrorMessageRepresentation("40111", "Error while retrieving user info from external service.")

}

