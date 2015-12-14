package model.OAuth

import java.util.UUID

import akka.actor.{Props, ActorRef, Actor}
import model.OAuth.OAuthCoordinator.{LoginResult, LoginRequest}
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._

/**
  * Created by darioalessandro on 12/14/15.
  */

case class User(name : String)

object JsonCat {

  //TODO: implement real parser
  implicit val userReader = new Reads[Option[User]] {

    def reads(json : JsValue) : JsResult[Option[User]] =  {
      json match {

        case j : JsObject =>
          JsSuccess(Some(User("not really parsed")))

        case j : JsArray =>
          JsError("sdf")
      }

    }

  }

  implicit val userWriter = new Writes[Option[User]] {
    def writes(user: Option[User]): JsValue = {
      user match {
        case Some(u) =>
          Json.obj("name" -> u.name)
        case None =>
          Json.obj()
      }
    }
  }

}

object OAuthCoordinator {
  case class LoginRequest(username : String, password : String, opId : UUID)
  case class LoginResult(user : Option[User], error : Option[Throwable], opId : UUID)
}

class OAuthCoordinator extends Actor {

  //TODO : Add logic so that this does not grow too much (backpressure)

  var loginRequests : Map[UUID, ActorRef] = Map[UUID, ActorRef]()

  var counter = 0

  override def receive : Receive = {
    case LoginRequest(username, password, opId) =>
      if(loginRequests.contains(opId))
        sender() ! LoginResult(None, error = Some(new Exception("request with UUID "+opId + "already exists")), opId)
      else {
        counter = counter + 1
        val worker = this.context.actorOf(Props(new OAuthWorker(counter % 2 == 0)), name = opId.toString)
        this.loginRequests =  this.loginRequests + (opId -> sender())
        worker ! LoginRequest(username, password, opId)
      }

    case LoginResult(user, error, opId) =>
      this.loginRequests.get(opId).foreach { requester =>
        requester ! LoginResult(user,error,opId)
      }

      this.loginRequests = this.loginRequests - opId
      context.stop(sender())

  }

}

class OAuthWorker(shouldFail : Boolean) extends Actor {

  override def receive : Receive = {
    case LoginRequest(username, password, opId) =>
      Thread.sleep(500)
      if (!shouldFail) {
        sender() ! LoginResult(Some(User(username)), None, opId)
      } else {
        sender() ! LoginResult(None, Some(new Exception("mock should fail")), opId)
      }

  }
}
