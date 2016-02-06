package controllers

import play.api.Routes
import play.api.mvc._
import play.api.routing.JavaScriptReverseRouter

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

class LoginUI extends Controller {

  def index(client_id: String, scope: String) = Action {
    Ok(views.html.enduser.index(client_id, scope))
  }

  def login = Action {
    Ok(views.html.enduser.login())
  }

  def redirect = Action { implicit request =>
    Ok(views.html.enduser.splash())
  }

  def jsRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("LoginUIRouter")(
        controllers.routes.javascript.LoginUI.login
      )
    ).as("text/javascript")
  }

}
