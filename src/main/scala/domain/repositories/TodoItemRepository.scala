package domain.repositories

import domain.models.TodoItem
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.parallel.mutable
import scala.concurrent.ExecutionContext.Implicits.global

class TodoItemRepository(implicit val executionContext: ExecutionContext) extends Repository[TodoItem] with CRUDOps[TodoItem] {
  val store = mutable.ParHashMap[UUID, TodoItem]()

  def getTodoItemsByUserId(userId: UUID): Future[Seq[TodoItem]] = Future {
    val result = for {
      (k, v) <- store if v.userId == userId
    } yield v
    result.toList
  }
}

object TodoItemRepository {
  def apply()(implicit executionContext: ExecutionContext): TodoItemRepository = new TodoItemRepository()
}
