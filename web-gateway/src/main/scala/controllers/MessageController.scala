package controllers

import java.util.{Date, UUID}

import com.livelygig.product.emailnotifications.api.EmailNotificationsService
import com.livelygig.product.message.api.{Message, MessageService}
import com.livelygig.product.message.api.{LiveMessagesRequest, Message, MessageService}
import com.livelygig.product.security.resource.ResourceClientSecurity
import play.api.Environment
import play.api.libs.json.JsValue
import play.api.mvc._

import scala.concurrent.ExecutionContext

/**
  * Created by shubham.k on 29-12-2016.
  */
class MessageController(messageService: MessageService, emailService: EmailNotificationsService)(implicit env: Environment, ec: ExecutionContext) extends Controller{
  def addMessage = Action.async { implicit rh =>
        messageService.addMessage
        //  .handleRequestHeader(ResourceClientSecurity.authenticate())
          .invoke(rh.body.asJson.get.as[Message])
          .map {
            msg => Ok("")
    }
  }

  def liveMsg = ???/*WebSocket.acceptOrResult[JsValue, JsValue] {
    implicit  request =>
      messageService.getLiveMessages().invoke(LiveMessagesRequest(Seq(userID.toString))).map {
        e => e.
      }
  }*/
}
