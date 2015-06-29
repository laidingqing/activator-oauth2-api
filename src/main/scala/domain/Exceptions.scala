package domain

case class RetrieveExternalAccessTokenException(code: String) extends Exception {
  val msg = s"Error while retrieving 'acess_token' from external service with authorization code: $code."
}

case class RetrieveExternalUserInfoException(accessToken: String) extends Exception {
  val msg = s"Error while retrieving user info external service with 'access_token': $accessToken."
}