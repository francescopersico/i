//import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
//import com.lightbend.lagom.scaladsl.api.{ServiceAcl, ServiceInfo}
//import com.lightbend.lagom.scaladsl.client.LagomServiceClientComponents
//import com.lightbend.lagom.scaladsl.server.LagomDevModeComponents
//import com.livelygig.product.message.api.MessageService
//import com.livelygig.product.user.api.UserService
//import play.api.ApplicationLoader.Context
//import play.api.i18n.I18nComponents
//import play.api.libs.ws.ahc.AhcWSComponents
//import com.softwaremill.macwire._
//import controllers.{Assets, Main, MessageController}
//import play.api.{ApplicationLoader, BuiltInComponentsFromContext, Mode}
//import router.Routes
//
//import scala.collection.immutable
//import scala.concurrent.ExecutionContext
//
//import com.softwaremill.macwire.MacwireMacros._
//import controllers.ApplicationController
//import controllers.Assets
//import controllers.SignInController
//import controllers.SignUpController
//import controllers.SocialAuthController
//import play.api.ApplicationLoader.Context
//import play.api._
//import play.api.http.HttpFilters
//import play.api.i18n._
//import play.api.libs.ws.WSClient
//import play.api.libs.ws.ahc.AhcWSClient
//import play.api.libs.ws.ning.NingWSComponents
//import play.api.mvc.{ EssentialAction, EssentialFilter }
//import play.api.routing.Router
//import play.filters.csrf.{ CSRFComponents, CSRFConfig, CSRFFilter }
//import play.filters.headers.SecurityHeadersComponents
//import router.Routes
//
//abstract class WebGateway (context: Context) extends BuiltInComponentsFromContext(context)
//  with I18nComponents
//  with AhcWSComponents
//  with LagomServiceClientComponents
//  with SeedAppComponents
//{
//
//  override lazy val serviceInfo: ServiceInfo = ServiceInfo(
//    "web-gateway",
//    Map(
//      "web-gateway" -> immutable.Seq(ServiceAcl.forPathRegex("(?!/api/).*"))
//    )
//  )
////  override httpErrorHandler = WebGatewayErrorHandler
//  override implicit lazy val executionContext: ExecutionContext = actorSystem.dispatcher
//  override lazy val router = { val prefix = "/"
//    wire[Routes]
//  }
//  implicit val env = context.environment
// // lazy val userService = serviceClient.implement[UserService]
//  lazy val messageService = serviceClient.implement[MessageService]
//  lazy val main = wire[Main]
//  lazy val messageController = wire[MessageController]
//  lazy val assets = wire[Assets]
//}
//
//class WebGatewayLoader extends ApplicationLoader {
//  override def load(context: Context) = context.environment.mode match {
//    case Mode.Dev =>
//      new WebGateway(context) with LagomDevModeComponents {}.application
//    case _ =>
//      new WebGateway(context) {
//        override def serviceLocator = NoServiceLocator
//      }.application
//  }
//}
//
//
//trait SeedAppComponents extends BuiltInComponents
//  with SeedAppModule
//  with I18nComponents
//  with CSRFComponents
//  with SecurityHeadersComponents {
////  lazy val assets: Assets = wire[Assets]
//  lazy val applicationController: ApplicationController = wire[ApplicationController]
//  lazy val socialAuthController: SocialAuthController = wire[SocialAuthController]
//  lazy val signupController: SignUpController = wire[SignUpController]
//  lazy val signinController: SignInController = wire[SignInController]
//  lazy val wsClient: WSClient = AhcWSClient()
////  lazy val router: Router = {
////    wire[Routes] withPrefix "/"
////  }
//
//  override lazy val httpFilters: Seq[EssentialFilter] = {
//    Seq(csrfFilter, securityHeadersFilter)
//  }
//}
//
