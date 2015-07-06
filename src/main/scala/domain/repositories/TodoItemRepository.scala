package domain.repositories

import domain.models.TodoItem
import java.util.UUID
import scala.concurrent.Future
import scala.collection.parallel.mutable
import scala.concurrent.ExecutionContext.Implicits.global

trait TodoItemRepository {
  def get(todoItemId: UUID): Future[Option[TodoItem]]

  def put(todoItem: TodoItem): Future[TodoItem]

  def delete(todoItemId: UUID): Future[Option[TodoItem]]

  def getTodoItemsByUserId(userId: UUID) : Future[Seq[TodoItem]]
}

class TodoItemRepositoryImpl extends TodoItemRepository {
  var memStore = mutable.ParHashMap[UUID, TodoItem]()

  def get(todoItemId: UUID) = Future {
    memStore.get(todoItemId)
  }

  def put(todoItem: TodoItem) = Future {
    val id = todoItem.todoItemId getOrElse java.util.UUID.randomUUID()
    val newTodoItem = todoItem.copy(todoItemId = Option(id))
    memStore += (id -> newTodoItem)
    newTodoItem
  }

  def delete(todoItemId: UUID) = Future {
    memStore.remove(todoItemId)
  }

  def getTodoItemsByUserId(userId: UUID) : Future[Seq[TodoItem]] = Future {
    val result = for {
      (k, v) <- memStore if v.userId == userId
    } yield v
    result.toList
  }
}
