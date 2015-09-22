package social

case class AccessTokenRepresentation(
  access_token: String,
  token_type: String,
  expires_in: Option[Long],
  refresh_token: Option[String])

trait AccountInfoRepresentation {
  def accountId: String
  def accountName: Option[String]
  def accountFirstname: Option[String]
  def accountLastname: Option[String]
  def accountEmail: String
}

case class FacebookAccountInfoRepresentation(id: String, name: Option[String], first_name: Option[String], last_name: Option[String], email: String) extends AccountInfoRepresentation {
  def accountId: String = id
  def accountName: Option[String] = name
  def accountFirstname: Option[String] = first_name
  def accountLastname: Option[String] = last_name
  def accountEmail: String = email
}

case class LiveAccountInfoRepresentation(id: String, name: Option[String], first_name: Option[String], last_name: Option[String], emails: LiveEmailsRepresentation) extends AccountInfoRepresentation {
  def accountId: String = id
  def accountName: Option[String] = name
  def accountFirstname: Option[String] = first_name
  def accountLastname: Option[String] = last_name
  def accountEmail: String = emails.account
}
case class LiveEmailsRepresentation(account: String)

case class GoogleAccountInfoRepresentation(id: String, name: Option[String], given_name: Option[String], family_name: Option[String], email: String, verified_email: Boolean) extends AccountInfoRepresentation {
  // Email must have been verified, if not we reject the authorization.
  require(verified_email, s"Unverified email: $email")

  def accountId: String = id
  def accountName: Option[String] = name
  def accountFirstname: Option[String] = given_name
  def accountLastname: Option[String] = family_name
  def accountEmail: String = email

}