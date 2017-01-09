package utils

import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler
import controllers.routes
import org.slf4j.LoggerFactory
import play.api.{Configuration, Mode, UsefulException}
import play.api.http.DefaultHttpErrorHandler
import play.api.i18n.Messages
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router
import play.core.SourceMapper

import scala.concurrent.Future

/**
 * A secured error handler.
 */
class ErrorHandler(
    env: play.api.Environment, config: Configuration,
    sourceMapper: Option[SourceMapper], router: Option[Router]
) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) with SecuredErrorHandler {

  private val log = LoggerFactory.getLogger(classOf[ErrorHandler])
  // 401 - Unauthorized
  override def onNotAuthenticated(implicit request: RequestHeader): Future[Result] = Future.successful {
    Unauthorized
  }

  // 403 - Forbidden
  override def onNotAuthorized(implicit request: RequestHeader): Future[Result] = Future.successful {
    Forbidden
  }

  // 404 - page not found error
  override def onNotFound(request: RequestHeader, message: String): Future[Result] = Future.successful {
    NotFound(env.mode match {
      case Mode.Prod => views.html.errors.notFound(request)(request2Messages(request))
      case _ => views.html.defaultpages.devNotFound(request.method, request.uri, Some(router.get))
    })
  }

  // 500 - internal server error
  override def onProdServerError(request: RequestHeader, exception: UsefulException) = Future.successful {
    log.error("Error occured with message:" + exception.description)
    InternalServerError(exception.description)
  }
}
