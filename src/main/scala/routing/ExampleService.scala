package routing

import akka.actor.ActorSystem
import spray.routing.Directives
import spray.http.StatusCodes
import spray.http.MediaTypes._

class ExampleServiceImpl()(implicit val actorSystem: ActorSystem) extends ExampleService

object ExampleService{
  def apply()(implicit actorSystem: ActorSystem) = new ExampleServiceImpl()(actorSystem)
}

trait ExampleService extends Directives {

  implicit val actorSystem: ActorSystem

  def route = {
    path("callback"){
      get{
        parameter('code){ code =>
          //TODO Call to auth service
          println(code)
          redirect("todolist", StatusCodes.PermanentRedirect)
        }
      }
    } ~
    path("todolist"){
      get {
        respondWithMediaType(`text/html`) {
          complete {
            com.whiteprompt.html.index().toString
          }
        }
      }
    }
  }
}