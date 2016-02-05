package controllers

/**
  * Created by darioalessandro on 2/5/16.
  */

import model.clientAPI.FAPI
import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

class ErrorHandler extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String) =
    FAPI(message, logout = false)(request)


  def onServerError(request: RequestHeader, exception: Throwable) =
    FAPI(exception.getMessage, logout = false)(request)
}

