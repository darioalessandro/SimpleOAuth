package controllers

import java.util.UUID

import model.clientAPI.{APIError, API}
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

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

class LoginAPI extends Controller {

  //TODO: add max length validation
  def authGrant() = Action(parse.form(authGrantForm)) { implicit request =>
    val token = AccessToken(UUID.randomUUID().toString, UUID.randomUUID().toString)
    API(token)(Json.writes[AccessToken], request)
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
