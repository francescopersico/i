//package mailer
//
//import com.livelygig.product.user.api.User
//import play.api.i18n.Messages
//import play.twirl.api.Html
//
//object Mailer {
//
//  implicit def html2String(html: Html): String = html.toString
//
//  def welcome(user: User, link: String)(implicit ms: MailService, m: Messages) {
//    ms.sendEmailAsync(user.email)(
//      subject = Messages("mail.welcome.subject"),
//      bodyHtml = "test body",
//      bodyText = "test text"
//    )
//  }
//
//  def forgotPassword(email: String, link: String)(implicit ms: MailService, m: Messages) {
//    ms.sendEmailAsync(email)(
//      subject = Messages("mail.forgotpwd.subject"),
//      bodyHtml = "test",
//      bodyText = "test"
//    )
//  }
//
//}