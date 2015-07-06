package domain.models

import java.util.UUID

case class TodoItem(
  todoItemId : Option[UUID],
  userId: UUID,
  title: String,
  description: String)
