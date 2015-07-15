package domain.models

import java.util.UUID

case class User(
  userId: Option[UUID],
  name: String,
  lastName: String,
  email: String,
  authToken: String)
