package controllers

import java.util.UUID
import javax.inject.Inject

import model.OAuth.{AccessToken, OAuthCoordinator}
import model.clientAPI.{API, FAPI}
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

@Singleton
class LoginAPI @Inject() (system: ActorSystem)  extends Controller {

  //TODO: add restart stategy
  val OAuthCoordinator = system.actorOf(Props[OAuthCoordinator])

  implicit val t: akka.util.Timeout = akka.util.Timeout(5 seconds)

  //TODO: add max length validation
  def authGrant() = Action.async(parse.form(authGrantForm)) { implicit request =>

    val authGrant = request.body

    val loginResult = OAuthCoordinator ? LoginRequest(authGrant.username,
                                                      authGrant.password,
                                                      authGrant.client_id,
                                                      authGrant.scope,
                                                      UUID.randomUUID())

    loginResult.map {
      case result: CreateTokenSuccess =>
        implicit val tokenparser = Json.writes[AccessToken]
        val success = Json.writes[CreateTokenSuccess]
        API(result)(success, request)

      case CreateTokenFailure(username, error, opId) =>
        API(error, logout = false)

      case loginError: LoginError =>
        API(loginError.error, logout = true)

      case _ =>
        API("unknown error", logout = false)

    }.recover {
      case e =>
        API(e, logout = false)
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
