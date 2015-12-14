package model.clientAPI

import play.api.libs.json._
import play.api.mvc.{WrappedRequest, Results, Result, Request}
import play.api.http.HeaderNames
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import play.api.mvc.RequestHeader

/**
  * The objective of this file is to encapsulate all the API responses.
  *
  * You'll notice that this framework always responds with a json with the form
  *
  *       {
  *         data : []
  *       }
  *
  *       if there is an error...
  *
  *       {
  *         error : {
  *           m : String,
  *           logout : Boolean
  *         },
  *         data : [] // Optional data
  *       }
  *
  *       Controllers should never use the raw Ok response.
  */


/**
  * Creates an API Error with the specified message.
  */

object Error {
  def apply(m:String,logout:Boolean)(implicit request : RequestHeader) = new APIError(m:String,logout:Boolean)
}

/**
  * Convenience method to create a future of an API Response, really useful when working with Async Actions
  */

object FAPI {

  def apply(t:String,logout:Boolean)(implicit request : RequestHeader) =
    Future{API(t,logout)}

  def apply(t:Throwable,logout:Boolean)(implicit request : RequestHeader) =
    Future{API(t,logout)}
}

/**
  * This is the APIResponse builder, it has several convenience methods so that your code is as small as possible.
  */

object API {

  def apply[A](t : Try[A])(implicit tis:Writes[A], request : RequestHeader) : Result = {
    t match {
      case Success(a) =>
        apply(a)

      case Failure(f) =>
        apply(APIError(f.getMessage, logout = false))
    }
  }

  def apply(t:String, logout : Boolean)(implicit request : RequestHeader) : Result =
    apply(APIError(t, logout))

  def apply(t:Throwable, logout : Boolean)(implicit request : RequestHeader) : Result =
    apply(APIError(t.getMessage, logout))

  def apply(t:JsError, logout : Boolean)(implicit request : RequestHeader) : Result =
    apply(APIError(t.toString, logout))

  def apply(error:APIError)(implicit request : RequestHeader) : Result = {
    val r = APIResponse(Some(error),None).toResult
    if(error.logout){
      r.withNewSession
    }else{
      r
    }
  }

  def apply[A](data:A)(implicit tis:Writes[A], request : RequestHeader) : Result = apply(Json.toJson(data))

  def apply(data:JsValue)(implicit request : RequestHeader)  : Result = APIResponse(None,Some(data)).toResult


  def apply(error:Option[APIError],data:JsValue)(implicit request : RequestHeader)  : Result = {
    val r = APIResponse(error, Some(data)).toResult
    error.map { e: APIError =>
      if (e.logout) r.withNewSession
      else r
    }.getOrElse {
        r
    }

  }
}

/**
  * Private classes
  */

object JsonParsers {

  implicit var formatAPIError = Json.format[APIError]

  implicit var formatAPIResponse = Json.format[APIResponse]

}

case class APIError(m:String,logout:Boolean)

case class APIResponse(error:Option[APIError],data:Option[JsValue]) {
  def toResult(implicit request : RequestHeader) : Result = {
    val r = Results.Ok(Json.toJson(this)(JsonParsers.formatAPIResponse)).withHeaders(HeaderNames.PRAGMA -> "no-cache", "Cache-Control" -> "no-cache")

    /*request match {

      case SecuredRequest(user:User,appSession : Session, request : Request[_]) =>
        Results.Ok(Json.toJson(this)(JsonParsers.formatAPIResponse)).withHeaders(HeaderNames.PRAGMA -> "no-cache", "Cache-Control" -> "no-cache")
          .withSession((C.sessionHeader, appSession.session))

      case _ =>
        r
    }
        */

    r


  }
  def toJson : JsValue = Json.toJson(this)(JsonParsers.formatAPIResponse)
}


