package domain.repositories

import domain.models.User
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.parallel.mutable

trait UserRepository {
  def get(userId: UUID): Future[Option[User]]

  def put(user: User): Future[User]

  def delete(userId: UUID): Future[Option[User]]
}

class UserRepositoryImpl(implicit executionContext: ExecutionContext) extends UserRepository {
  var memStore = mutable.ParHashMap[UUID, User]()

  def get(userId: UUID) = Future {
    memStore.get(userId)
  }

  def put(user: User) = Future {
    val id = user.userId getOrElse java.util.UUID.randomUUID()
    val newUser = user.copy(userId = Option(id))
    memStore += (id -> newUser)
    newUser
  }

  def delete(userId: UUID) = Future {
    memStore.remove(userId)
  }
}