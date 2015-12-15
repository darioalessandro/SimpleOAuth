package model.OAuth

import java.util.UUID

import akka.actor.{Props, ActorRef, Actor}
import com.datastax.driver.core.{Session, Cluster}
import model.OAuth.OAuthCoordinator.{LoginResult, LoginRequest}
import scala.collection.JavaConverters._

/**
  * Created by darioalessandro on 12/14/15.
  */

case class User(name : String)

object OAuthCoordinator {
  case class LoginRequest(username : String, password : String, opId : UUID)
  case class LoginResult(user : Option[User], error : Option[Throwable], opId : UUID)
}

class OAuthCoordinator extends Actor {

  //TODO : Add logic so that this does not grow too much (backpressure)

  var loginRequests : Map[UUID, ActorRef] = Map[UUID, ActorRef]()

  val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
  val session = cluster.connect("authentication")

  override def receive : Receive = {
    case LoginRequest(username, password, opId) =>
      if(loginRequests.contains(opId))
        sender() ! LoginResult(None, error = Some(new Exception("request with UUID "+opId + "already exists")), opId)
      else {
        val worker = this.context.actorOf(Props(new OAuthWorker(session)), name = opId.toString)
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

class OAuthWorker(session : Session) extends Actor {

  override def receive : Receive = {

    case LoginRequest(username, password, opId) =>
      val results = session.execute(s"""select * from users where username='$username'""").asScala.toList
      sender() !  results.headOption.map { row =>
        LoginResult(Some(User(username)), None, opId)
      }.getOrElse {
        LoginResult(None, Some(new Exception("no results for specified user")), opId)
      }

    case a =>
      println("message not handled "+a)
  }
}

class MockOAuthWorker(shouldFail : Boolean) extends Actor {

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
