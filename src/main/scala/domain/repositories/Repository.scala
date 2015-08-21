package domain.repositories

import java.util.UUID

import domain.models.Entity

import scala.collection.parallel.mutable
import scala.concurrent.{ExecutionContext, Future}

trait Repository[T <: Entity] {
  implicit val executionContext: ExecutionContext

  val store: mutable.ParHashMap[UUID, T]
}

trait CRUDOps[T <: Entity] {
    self: Repository[T] =>

  def create(e: T): Future[T] = Future {
    store += (e.id -> e)
    e
  }

  def retrieve(id: UUID): Future[Option[T]] = Future {
    store.get(id)
  }

  def update(e: T): Future[T] = Future {
    store += (e.id -> e)
    e
  }

  def delete(id: UUID): Future[Option[T]] = Future {
    store.remove(id)
  }

}