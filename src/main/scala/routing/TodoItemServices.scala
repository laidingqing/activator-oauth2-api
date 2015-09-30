package routing

import akka.actor.ActorSystem
import domain.models.{TodoItemDto, TodoItem}
import domain.repositories.TodoItemRepository
import domain.converters.TodoItemConverters._
import java.util.UUID
import org.json4s.DefaultFormats
import org.json4s.ext.JavaTypesSerializers
import scala.concurrent.ExecutionContext
import spray.httpx.Json4sSupport
import spray.routing._

class TodoItemServicesImpl(val todoItemRepository: TodoItemRepository)(implicit val actorSystem: ActorSystem) extends TodoItemServices

object TodoItemServices {
  def apply(todoItemRepository: TodoItemRepository)(implicit actorSystem: ActorSystem): TodoItemServices = {
    new TodoItemServicesImpl(todoItemRepository)(actorSystem)
  }
}

trait TodoItemServices extends Directives with Json4sSupport {

  implicit def json4sFormats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all ++ JavaTypesSerializers.all
  implicit val executionContext: ExecutionContext = ExecutionContext.global

  def todoItemRepository: TodoItemRepository

  def route = pathPrefix("user" / "me" / "todo-items") {
    pathEnd {
      post {
        entity(as[TodoItemDto]) { todoItemDto =>
          complete {
            val todoItem = TodoItem(
              UUID.fromString("1"),
              UUID.fromString("1"),
              todoItemDto.title,
              todoItemDto.description)

            todoItemRepository.create(todoItem)
          }
        }
      } ~
      get {
        complete {
          todoItemRepository.getTodoItemsByUserId(UUID.fromString("1")) map { items =>
            items map toTodoItemDto
          }
        }
      }
    } ~ path(Segment) { id =>
      get {
        complete({
          todoItemRepository.retrieve(UUID.fromString(id)) map {_ map toTodoItemDto}
        })
      } ~
        delete {
          complete({
            todoItemRepository.delete(UUID.fromString(id))
          })
        }
    }
  }

}

