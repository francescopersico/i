package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.providers._
import forms.SignUpForm
import java.io.File
import models.User
import models.services.{AuthTokenService, UserService}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.mailer.{AttachmentData, AttachmentFile, Email, MailerClient}
import play.api.mvc.Controller
import play.api.libs.mailer._
import java.io.File
import org.apache.commons.mail.EmailAttachment
import utils.auth.DefaultEnv

import scala.concurrent.Future


/**
 * The `Sign Up` controller.
 *
 * @param messagesApi            The Play messages API.
 * @param silhouette             The Silhouette stack.
 * @param userService            The user service implementation.
 * @param authInfoRepository     The auth info repository implementation.
 * @param authTokenService       The auth token service implementation.
 * @param avatarService          The avatar service implementation.
 * @param passwordHasherRegistry The password hasher registry.
 * @param mailerClient           The mailer client.
 * @param webJarAssets           The webjar assets implementation.
 */
class SignUpController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  authTokenService: AuthTokenService,
  avatarService: AvatarService,
//  passwordHasherRegistry: PasswordHasherRegistry,
  passwordHasher: PasswordHasher,
  mailerClient: MailerClient,
  implicit val webJarAssets: WebJarAssets)
  extends Controller with I18nSupport {

  /**
   * Views the `Sign Up` page.
   *
   * @return The result to display.
   */
  def view = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.signUp(SignUpForm.form)))
  }

  /**
   * Handles the submitted form.
   *
   * @return The result to display.
   */
  def submit = silhouette.UnsecuredAction.async { implicit request =>
    SignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signUp(form))),
      data => {
        val result = Redirect(routes.SignUpController.view()).flashing("info" -> Messages("sign.up.email.sent", data.email))
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            val url = routes.SignInController.view().absoluteURL()
            val cid = "1234"
            val email = Email(
              subject = Messages("email.already.signed.up.subject"),
              from = Messages("email.from"),
              to = Seq(data.email),
              // adds attachment
              attachments = Seq(
               // AttachmentFile("attachment.pdf", new File("/some/path/attachment.pdf")),
                // adds inline attachment from byte array
               // AttachmentData("data.txt", "data".getBytes, "text/plain", Some("Simple data"), Some(EmailAttachment.INLINE)),
                // adds cid attachment
                AttachmentFile("LivelyGig-logo-symbol.svg", new File("images/LivelyGig-logo-symbol.svg"), contentId = Some(cid))
              ),
              // sends text, HTML or both...
//              bodyText = Some("A text message"),
//              bodyHtml = Some(s"""<html><body><p>An <b>html</b> message with cid <img src="cid:$cid"></p></body></html>""")


              bodyText = Some(views.txt.emails.alreadySignedUp(user, url).body),
              bodyHtml = Some(views.html.emails.alreadySignedUp(user, url).body)
            )
           /* mailerClient.send(Email(
              subject = Messages("email.already.signed.up.subject"),
              from = Messages("email.from"),
              to = Seq(data.email),
              bodyText = Some(views.txt.emails.alreadySignedUp(user, url).body),
              bodyHtml = Some(views.html.emails.alreadySignedUp(user, url).body)
            ))*/
            mailerClient.send(email)

            Future.successful(result)
          case None =>
            val authInfo = passwordHasher.hash(data.password)
            val user = User(
              userID = UUID.randomUUID(),
              loginInfo = loginInfo,
              firstName = Some(data.firstName),
              lastName = Some(data.lastName),
              fullName = Some(data.firstName + " " + data.lastName),
              email = Some(data.email),
              avatarURL = None,
              activated = false
            )
            for {
              avatar <- avatarService.retrieveURL(data.email)
              user <- userService.save(user.copy(avatarURL = avatar))
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
              authToken <- authTokenService.create(user.userID)
            } yield {
              val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()
              val cid = "1234"
              mailerClient.send(Email(
                subject = Messages("email.sign.up.subject"),
                from = Messages("email.from"),
                to = Seq(data.email),
                attachments = Seq(
                  // AttachmentFile("attachment.pdf", new File("/some/path/attachment.pdf")),
                  // adds inline attachment from byte array
                  // AttachmentData("data.txt", "data".getBytes, "text/plain", Some("Simple data"), Some(EmailAttachment.INLINE)),
                  // adds cid attachment
                  AttachmentFile("LivelyGig-logo-symbol.svg", new File("D:/ClientUI/Keeper/i/web-gateway/src/main/assets/images/LivelyGig-logo-symbol.svg"), contentId = Some(cid))
                ),
                bodyText = Some(views.txt.emails.signUp(user, url).body),
                bodyHtml = Some(views.html.emails.signUp(user, url).body)
              ))

              silhouette.env.eventBus.publish(SignUpEvent(user, request))
              result
            }
        }
      }
    )
  }
}