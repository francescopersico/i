//package controllers
//
//import java.util.UUID
//
//import com.livelygig.product.security.ClientSecurity.authenticate
//import com.livelygig.product.user.api.{User, UserService}
//import org.slf4j.LoggerFactory
//import play.api.mvc._
//import play.api.Environment
//import play.api.libs.json.Json
//
//import scala.concurrent.{ExecutionContext}
//
//class Main(userService: UserService)(implicit env: Environment, ec: ExecutionContext) extends Controller {
//  private val log = LoggerFactory.getLogger(classOf[Main])
//  def index = Action {
//    Ok(views.html.index("LivelyGig"))
//  }
//
//  def logging = Action(parse.anyContent) {
//    implicit request =>
//      request.body.asJson.foreach { msg =>
//        println(s"Application - CLIENT - $msg")
//      }
//      Ok("")
//  }
//
//
//  def signup = Action.async { implicit rh =>
//    userService.signup
//      .invoke(rh.body.asJson.get.as[User]).map {
//        _ => Ok("")
//      }
//  }
//
//  def login = Action.async { implicit rh =>
////    if (true) throw new Exception("an error!")
//    userService.login.invoke(rh.body.asJson.get.as[User]).map {
//      user => Ok(Json.toJson(user))
//    }
//  }
//
//}