package controllers

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

class LoginAPI extends Controller {

  def authGrant = Action {
    Ok("Cool")
  }

}
