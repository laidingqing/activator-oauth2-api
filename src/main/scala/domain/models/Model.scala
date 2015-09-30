package domain.models

import java.util.UUID

import org.joda.time.DateTime

trait Entity {
  val id: UUID
}

case class User(
  id: UUID,
  name: Option[String],
  lastname: Option[String],
  email: String,
  externalAccounts: Seq[ExternalAccount]) extends Entity

case class ExternalAccount(
  externalAccountType: ExternalAccountType.Value,
  externalAccountId: String,
  accessToken: String)

object ExternalAccountType extends Enumeration {
  val Facebook = Value("Facebook")
  val Google = Value("Google")
  val Live = Value("Live")
}

case class OAuthToken(
  id: UUID,
  accessToken: String,
  issuedAt: DateTime,
  isValid: Boolean,
  userRef: UUID) extends Entity

case class TodoItem(
  id: UUID,
  userId: UUID,
  title: String,
  description: String) extends Entity

case class TodoItemDto(title: String, description: String)