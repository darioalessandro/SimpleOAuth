package model.OAuth

import java.util.UUID

import akka.actor.{Props, ActorRef, Actor}
import com.datastax.driver.core.{Session, Cluster}
import model.OAuth.OAuthCoordinator._
import scala.collection.JavaConverters._
import scala.util.{Success, Try}

/**
  * Created by darioalessandro on 12/14/15.
  */

case class User(name : String)

case class AccessToken(token: String, refreshToken : String)

object OAuthCoordinator {
  class OAuthCoordinatorMessage()
  case class LoginRequest(username : String, password : String, clientId : String, scope : String, opId : UUID) extends OAuthCoordinatorMessage
  case class LoginError(error : Throwable, opId : UUID, requestor : ActorRef) extends OAuthCoordinatorMessage
  class CreateTokenResult() extends OAuthCoordinatorMessage
  case class CreateTokenSuccess(username : String, token : AccessToken, opId : UUID, callbackUrl : String) extends CreateTokenResult
  case class CreateTokenFailure(username : String, error : Exception, opId : UUID) extends CreateTokenResult

  case class LoginRequestInternal(username : String, password : String, clientId : String, scope : String, opId : UUID, requestor : ActorRef)
  case class CreateToken(username : String, clientId : String, opId : UUID, requestor : ActorRef, callbackUrl : String)

}

class OAuthCoordinator extends Actor with akka.actor.ActorLogging {

  //TODO : Add logic so that this does not grow too much (backpressure)

  //var loginRequests : Map[UUID, ActorRef] = Map[UUID, ActorRef]()

  val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
  val session = cluster.connect("authentication")

  override def receive : Receive = {
    case LoginRequest(username, password, clientId, scope, opId) =>
        val worker = this.context.actorOf(Props(new OAuthWorker(session)), name = opId.toString)
        worker ! LoginRequestInternal(username, password, clientId, scope, opId, sender())

    case tokenResult : CreateTokenResult =>
      this.context.stop(sender())

    case loginError : LoginError =>
      this.context.stop(sender())
  }

}

class OAuthWorker(session : Session) extends Actor with akka.actor.ActorLogging {

  override def receive : Receive = {

    case LoginRequestInternal(username, password, clientId, scope, opId, requester) =>
      val getUserQuery = session.execute(s"""select * from users where username='$username'""").asScala.toList
      val redirectUrlQuery = session.execute(s"""select * from clients where client_id='$clientId'""").asScala.toList

      (getUserQuery.headOption,redirectUrlQuery.headOption) match {
        case (Some(userRow),Some(redirectURLRow)) =>
          val redirectURL = redirectURLRow.get("callback_url", classOf[String])
          val tokenCreator = this.context.actorOf(Props(new TokenCreator(session)), name = opId.toString)
          tokenCreator ! CreateToken(username, clientId, opId, requester, redirectURL)

        case _ =>
          val error = LoginError(new Exception("credentials error"), opId, requester)
          requester ! error
          this.context.parent ! error
      }

    case tokenResult : CreateTokenResult =>
      this.context.parent ! tokenResult

  }
}

class TokenCreator(session : Session) extends Actor with akka.actor.ActorLogging {

  override def receive : Receive = {
    case CreateToken(username : String, clientId : String, opId : UUID, requester : ActorRef, callbackUrl : String) =>
      val token = UUID.randomUUID().toString
      val refreshToken = UUID.randomUUID().toString
      val result = try {
        session.execute(s"INSERT INTO tokens(username, otoken, refreshToken, creation) VALUES ('$username','$token','$refreshToken', dateof(now()))")
        CreateTokenSuccess(username, AccessToken(token, refreshToken), opId, callbackUrl)
      }catch {
        case e : Exception  =>
          CreateTokenFailure(username, e, opId)
      }

      requester ! result
      sender() ! result
  }

}
