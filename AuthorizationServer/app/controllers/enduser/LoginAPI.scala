package controllers

import java.util.UUID
import javax.inject.Inject

import model.OAuth.OAuthCoordinator
import model.OAuth.OAuthCoordinator.LoginResult
import model.clientAPI.{ FAPI, API}
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import akka.actor._
import akka.pattern.ask
import scala.concurrent.Future
import scala.concurrent.duration._
import model.OAuth.OAuthCoordinator._
import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Singleton

/**
  * Created by darioalessandro on 12/14/15.
  *
  *
1.2.  Protocol Flow (modified RFC 6749)

     +--------+                               +---------------+
     |        |--(A)- Authorization Request ->|               |
     |        |                               |               |
     |        |<-(B)-- Authorization Grant ---|               |
     |        |                               | Authorization |
     |        |                               |     Server    |
     |        |                               |               |
     |        |--(C)-- Authorization Grant -->|               |
     | Client |                               |               |
     |        |<-(D)----- Access Token -------|               |
     |        |                               +---------------+
     |        |
     |        |                               +---------------+
     |        |--(E)----- Access Token ------>|    Resource   |
     |        |                               |     Server    |
     |        |<-(F)--- Protected Resource ---|               |
     +--------+                               +---------------+
  */

case class AuthorizationGrantData(client_id : String, username : String, password : String, scope : String)

case class AccessToken(token: String, refreshToken : String)

@Singleton
class LoginAPI @Inject() (system: ActorSystem)  extends Controller {

  val OAuthCoordinator = system.actorOf(Props[OAuthCoordinator])

  //TODO: add max length validation
  def authGrant() = Action.async(parse.form(authGrantForm)) { implicit request =>

    implicit val t : akka.util.Timeout = akka.util.Timeout(5 seconds)
    val loginResult : Future[Any] = OAuthCoordinator ? LoginRequest(request.body.username, request.body.password, UUID.randomUUID())

    loginResult match {
      case l : Future[LoginResult] =>
        l.map { l =>
          l.error.map { e =>
            API(e, logout = false)
          }.getOrElse {
            val token = AccessToken(UUID.randomUUID().toString, UUID.randomUUID().toString)
            API(token)(Json.writes[AccessToken], request)
          }
        }.recoverWith {
          case e : Throwable => FAPI(e, logout = false)
        }
      case _ =>
        FAPI("fuck", logout = false)
    }
  }

  val authGrantForm = Form(
    mapping(
      "client_id" -> text,
      "username" -> text,
      "password" -> text,
      "scope" -> text
    )(AuthorizationGrantData.apply)(AuthorizationGrantData.unapply)
  )
}
