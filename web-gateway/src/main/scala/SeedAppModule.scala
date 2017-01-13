import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.actions.{UnsecuredRequestHandlerBuilder, _}
import com.mohiva.play.silhouette.api.util.{Clock, PlayHTTPLayer}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticator, CookieAuthenticatorService, CookieAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.providers.{CredentialsProvider, SocialProviderRegistry}
import com.mohiva.play.silhouette.impl.services.GravatarService
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.mohiva.play.silhouette.persistence.memory.daos.PasswordInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import models.daos.{AuthTokenDAOImpl, UserDAOImpl}
import models.services.{AuthTokenServiceImpl, UserServiceImpl}
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.ws.WSClient
import utils.auth.{CustomSecuredErrorHandler, CustomUnsecuredErrorHandler, DefaultEnv}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import play.api.Configuration


trait SeedAppModule {

  import play.api.libs.concurrent.Execution.Implicits._

  import com.softwaremill.macwire.MacwireMacros._

  def langs: Langs

  def messagesApi: MessagesApi

  lazy val fingerprintGenerator = new DefaultFingerprintGenerator(false)
  lazy val idGenerator = new SecureRandomIDGenerator()

  def configuration: Configuration

  lazy val userService = new UserServiceImpl(new UserDAOImpl)
  lazy val authTokenService = new AuthTokenServiceImpl(new AuthTokenDAOImpl,clock)//(new UserDAOImpl)

  lazy val authenticatorService: AuthenticatorService[CookieAuthenticator] = {
    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    new CookieAuthenticatorService(config, None,fingerprintGenerator, idGenerator, clock)
  }
  lazy val eventBus = EventBus()

  private lazy val env: Environment[DefaultEnv] = Environment[DefaultEnv](
    userService, authenticatorService, List(), eventBus
  )

  lazy val securedErrorHandler: SecuredErrorHandler = wire[CustomSecuredErrorHandler]
  lazy val unSecuredErrorHandler: UnsecuredErrorHandler = wire[CustomUnsecuredErrorHandler]

  lazy val securedAction: SecuredAction = new DefaultSecuredAction(new DefaultSecuredRequestHandler(securedErrorHandler))
  lazy val unsecuredAction: UnsecuredAction = new DefaultUnsecuredAction(new DefaultUnsecuredRequestHandler(unSecuredErrorHandler))

  lazy val userAwareAction = new DefaultUserAwareAction(new DefaultUserAwareRequestHandler)

  lazy val passwordDao = new PasswordInfoDAO
  lazy val authInfoRepository = new DelegableAuthInfoRepository(passwordDao)
  lazy val passwordHasher = new BCryptPasswordHasher()

  lazy val credentialsProvider = new CredentialsProvider(authInfoRepository, passwordHasher, List(passwordHasher))

  lazy val socialProviderRegistry = new SocialProviderRegistry(List())
  lazy val clock = Clock()

  def wsClient: WSClient

  lazy val httpLayer = new PlayHTTPLayer(wsClient)
  lazy val avatarService = new GravatarService(httpLayer)

  lazy val silhouette: Silhouette[DefaultEnv] = wire[SilhouetteProvider[DefaultEnv]]

}
