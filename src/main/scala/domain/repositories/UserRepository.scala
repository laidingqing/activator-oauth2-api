package domain.repositories

import java.util.UUID

import domain.models.{ExternalAccountType, User}

import scala.collection.parallel.mutable
import scala.concurrent.{ExecutionContext, Future}

class UserRepository(implicit val executionContext: ExecutionContext) extends Repository[User] with CRUDOps[User] {
  val store = mutable.ParHashMap[UUID, User]()

  def findByExternalAccount(externalAccountType: ExternalAccountType.Value, externalAccountId: String): Future[Option[User]] = Future {
    store.find{ case(uuid, user) =>
      user.externalAccounts.find(ea =>
        ea.externalAccountType.equals() && ea.externalAccountId.equals(externalAccountId)).isDefined}.map(_._2)
  }
}

object UserRepository {
  def apply()(implicit executionContext: ExecutionContext): UserRepository = new UserRepository()
}