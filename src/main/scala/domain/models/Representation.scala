package domain.models

import social.{GoogleAccountInfoRepresentation, LiveAccountInfoRepresentation, FacebookAccountInfoRepresentation}

case class ExternalUserInfo(id: String, name: Option[String], firstname: Option[String], lastname: Option[String], email: String)

object ExternalUserInfo {
  implicit def toExternalUserInfo(facebookUserInfoRepresentation: FacebookAccountInfoRepresentation): ExternalUserInfo = {
    ExternalUserInfo(
      facebookUserInfoRepresentation.id,
      facebookUserInfoRepresentation.name,
      facebookUserInfoRepresentation.first_name,
      facebookUserInfoRepresentation.last_name,
      facebookUserInfoRepresentation.email
    )
  }

  implicit def toExternalUserInfo(liveUserInfoRepresentation: LiveAccountInfoRepresentation): ExternalUserInfo = {
    ExternalUserInfo(
      liveUserInfoRepresentation.id,
      liveUserInfoRepresentation.name,
      liveUserInfoRepresentation.first_name,
      liveUserInfoRepresentation.last_name,
      liveUserInfoRepresentation.emails.account
    )
  }

  implicit def toExternalUserInfo(googleUserInfoRepresentation: GoogleAccountInfoRepresentation): ExternalUserInfo = {
    ExternalUserInfo(
      googleUserInfoRepresentation.id,
      googleUserInfoRepresentation.name,
      googleUserInfoRepresentation.given_name,
      googleUserInfoRepresentation.family_name,
      googleUserInfoRepresentation.email
    )
  }

}

case class ErrorMessageRepresentation(code: String, message: String) extends Representation
case class ExternalUserInfoMessageErrorRepresentation(code: String, message: String, externalAccountInfo: ExternalUserInfo) extends Representation

object ErrorMessageRepresentation {
  val FailedAccessTokenRetrievalFromExternalService = ErrorMessageRepresentation("40110", "Error while retrieving 'access_token' from external service.")
  val FailedUserInfoRetrievalFromExternalService = ErrorMessageRepresentation("40111", "Error while retrieving user info from external service.")

}

