package domain.repositories

import java.util.UUID

import domain.models.OAuthToken

import scala.collection.parallel.mutable
import scala.concurrent.ExecutionContext

class OAuthTokenRepository(implicit val executionContext: ExecutionContext) extends Repository[OAuthToken] with CRUDOps[OAuthToken] {
  val store = mutable.ParHashMap[UUID, OAuthToken]()
}

object OAuthTokenRepository {
  def apply()(implicit executionContext: ExecutionContext): OAuthTokenRepository = new OAuthTokenRepository()
}