/**
 * Created by luos on 04/04/16.
 */


import controllers.ApplicationController
import controllers.Assets
import controllers.SignInController
import controllers.SignUpController
import controllers.SocialAuthController
import controllers.WebJarAssets
import play.api.ApplicationLoader.Context
import play.api._
import play.api.http.HttpFilters
import play.api.i18n._
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.AhcWSClient
import play.api.libs.ws.ning.NingWSComponents
import play.api.mvc.{EssentialAction, EssentialFilter}
import play.api.routing.Router
import play.filters.csrf.{CSRFComponents, CSRFConfig, CSRFFilter}
import play.filters.headers.SecurityHeadersComponents
import router.Routes
import com.softwaremill.macwire.MacwireMacros._
import controllers.{ActivateAccountController, ChangePasswordController, ForgotPasswordController, ResetPasswordController}
import play.api.libs.mailer.MailerComponents

/**
 * Application loader that wires up the application dependencies using Macwire
 */
class SeedAppLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    (new BuiltInComponentsFromContext(context) with SeedAppComponents).application
  }
}

trait SeedAppComponents extends BuiltInComponents
  with SeedAppModule
  with I18nComponents
  with CSRFComponents
  with MailerComponents
  with SecurityHeadersComponents {
  lazy val assets: Assets = wire[Assets]
  lazy val applicationController: ApplicationController = wire[ApplicationController]
  lazy val socialAuthController: SocialAuthController = wire[SocialAuthController]
  lazy val signupController: SignUpController = wire[SignUpController]
  lazy val signinController: SignInController = wire[SignInController]
  lazy val webjarAssets: WebJarAssets = wire[WebJarAssets]
  lazy val forgotPasswordController: ForgotPasswordController = wire[ForgotPasswordController]
  lazy val resetPasswordController: ResetPasswordController = wire[ResetPasswordController]
  lazy val changePasswordController: ChangePasswordController = wire[ChangePasswordController]
  lazy val activateAccountController: ActivateAccountController = wire[ActivateAccountController]
  lazy val wsClient: WSClient = AhcWSClient()
  lazy val router: Router = {
    wire[Routes] withPrefix "/"
  }

  override lazy val httpFilters: Seq[EssentialFilter] = {
    Seq(csrfFilter, securityHeadersFilter)
  }
}
