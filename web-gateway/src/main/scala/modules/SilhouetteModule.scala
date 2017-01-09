package modules

import com.livelygig.product.user.api.{User, UserService}
import com.mohiva.play.silhouette.api.crypto.{CookieSigner, Crypter, CrypterAuthenticatorEncoder}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus}
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth1._
import com.mohiva.play.silhouette.impl.providers.oauth1.secrets.{CookieSecretProvider, CookieSecretSettings}
import com.mohiva.play.silhouette.impl.providers.oauth1.services.PlayOAuth1Service
import com.mohiva.play.silhouette.impl.providers.oauth2._
import com.mohiva.play.silhouette.impl.providers.oauth2.state.{CookieStateProvider, CookieStateSettings, DummyStateProvider}
import com.mohiva.play.silhouette.impl.providers.openid.YahooProvider
import com.mohiva.play.silhouette.impl.providers.openid.services.PlayOpenIDService
import com.mohiva.play.silhouette.impl.services._
import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.password.BCryptPasswordHasher
import com.softwaremill.macwire._
import play.api.Configuration
import play.api.cache.CacheApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.openid.OpenIdClient
import play.api.libs.ws.WSClient
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

trait SilhouetteModule {
  def configuration: Configuration

  def defaultCacheApi: CacheApi

  def wsClient: WSClient

  def openIdClient: OpenIdClient

  lazy val clock = Clock()
  lazy val eventBus = EventBus()
  lazy val fingerprintGenerator = new DefaultFingerprintGenerator(false)
  lazy val idGenerator = new SecureRandomIDGenerator
  lazy val passwordHasher = new BCryptPasswordHasher
  lazy val cacheLayer = wire[PlayCacheLayer]
  lazy val authenticatorService = wireWith(SilhouetteAuthenticatorService.apply _)

  lazy val httpLayer = wire[PlayHTTPLayer]
  lazy val silhouetteEnvironment = wireWith(SilhouetteEnvironment.apply _)
  lazy val settings = GravatarServiceSettings()
  lazy val avatarService = wire[GravatarService]
  lazy val tokenSecretProvider = wireWith(SilhouetteOAuth1TokenSecretProvider.apply _)
  lazy val stateProvider = wireWith(SilhouetteOAuth2StateProvider.apply _)
  lazy val facebookProvider = wireWith(SilhouetteFacebookProvider.apply _)
  lazy val clefProvider = wireWith(SilhouetteClefProvider.apply _)
  lazy val xingProvider = wireWith(SilhouetteXingProvider.apply _)
  lazy val twitterProvider = wireWith(SilhouetteTwitterProvider.apply _)
  lazy val vKProvider = wireWith(SilhouetteVKProvider.apply _)
  lazy val googleProvider = wireWith(SilhouetteGoogleProvider.apply _)
  lazy val yahooProvider = wireWith(SilhouetteYahooProvider.apply _)
  lazy val socialProviderRegistry = wireWith(SilhouetteSocialProviderRegistry.apply _)
//  lazy val authInfoRepository = wireWith(SilhouetteAuthInfoRepository.apply _)
//  lazy val credentialsProvider: CredentialsProvider = wireWith(SilhouetteCredentialsProvider.apply _)

  object SilhouetteAuthenticatorService {
    def apply(
               cookieSigner: CookieSigner,
               crypter: Crypter,
               fingerprintGenerator: FingerprintGenerator,
               idGenerator: IDGenerator,
               configuration: Configuration,
               clock: Clock
             ): AuthenticatorService[CookieAuthenticator] = {
      val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
      val encoder = new CrypterAuthenticatorEncoder(crypter)
      new CookieAuthenticatorService(config, None, cookieSigner, encoder, fingerprintGenerator, idGenerator, clock)
    }
  }

  object SilhouetteEnvironment {
    def apply(
               userService: UserService,
               authenticatorService: AuthenticatorService[CookieAuthenticator],
               eventBus: EventBus
             ): Environment[User, CookieAuthenticator] = {
      Environment(userService, authenticatorService, Seq(), eventBus)
    }
  }

  object SilhouetteOAuth1TokenSecretProvider {
    def apply(clock: Clock, configuration: Configuration): OAuth1TokenSecretProvider = {
      val settings = configuration.underlying.as[CookieSecretSettings]("silhouette.oauth1TokenSecretProvider")
      new CookieSecretProvider(settings, clock)
    }
  }

  object SilhouetteOAuth2StateProvider {
    def apply(
               idGenerator: IDGenerator, clock: Clock, configuration: Configuration
             ): OAuth2StateProvider = {
      val settings = configuration.underlying.as[CookieStateSettings]("silhouette.oauth2StateProvider")
      new CookieStateProvider(settings, idGenerator, clock)
    }
  }

  object SilhouetteFacebookProvider {
    def apply(
               httpLayer: HTTPLayer, stateProvider: OAuth2StateProvider, configuration: Configuration
             ): FacebookProvider = {
      val settings = configuration.underlying.as[OAuth2Settings]("silhouette.facebook")
      new FacebookProvider(httpLayer, stateProvider, settings)
    }
  }

  object SilhouetteGoogleProvider {
    def apply(
               httpLayer: HTTPLayer, stateProvider: OAuth2StateProvider, configuration: Configuration
             ): GoogleProvider = {
      val settings = configuration.underlying.as[OAuth2Settings]("silhouette.google")
      new GoogleProvider(httpLayer, stateProvider, settings)
    }
  }

  object SilhouetteVKProvider {
    def apply(
               httpLayer: HTTPLayer, stateProvider: OAuth2StateProvider, configuration: Configuration
             ): VKProvider = {
      val settings = configuration.underlying.as[OAuth2Settings]("silhouette.vk")
      new VKProvider(httpLayer, stateProvider, settings)
    }
  }

  object SilhouetteTwitterProvider {
    def apply(
               httpLayer: HTTPLayer, tokenSecretProvider: OAuth1TokenSecretProvider, configuration: Configuration
             ): TwitterProvider = {
      val settings = configuration.underlying.as[OAuth1Settings]("silhouette.twitter")
      new TwitterProvider(httpLayer, new PlayOAuth1Service(settings), tokenSecretProvider, settings)
    }
  }

  object SilhouetteXingProvider {
    def apply(
               httpLayer: HTTPLayer, tokenSecretProvider: OAuth1TokenSecretProvider, configuration: Configuration
             ): XingProvider = {
      val settings = configuration.underlying.as[OAuth1Settings]("silhouette.xing")
      new XingProvider(httpLayer, new PlayOAuth1Service(settings), tokenSecretProvider, settings)
    }
  }

  object SilhouetteYahooProvider {
    def apply(
               cacheLayer: CacheLayer, httpLayer: HTTPLayer, client: OpenIdClient, configuration: Configuration
             ): YahooProvider = {
      val settings = configuration.underlying.as[OpenIDSettings]("silhouette.yahoo")
      new YahooProvider(httpLayer, new PlayOpenIDService(client, settings), settings)
    }
  }

  object SilhouetteClefProvider {
    def apply(httpLayer: HTTPLayer, configuration: Configuration): ClefProvider = {
      val settings = configuration.underlying.as[OAuth2Settings]("silhouette.clef")
      new ClefProvider(httpLayer, new DummyStateProvider, settings)
    }
  }

  object SilhouetteSocialProviderRegistry {
    def apply(
               facebookProvider: FacebookProvider,
               googleProvider: GoogleProvider,
               vkProvider: VKProvider,
               clefProvider: ClefProvider,
               twitterProvider: TwitterProvider,
               xingProvider: XingProvider,
               yahooProvider: YahooProvider
             ): SocialProviderRegistry = {
      SocialProviderRegistry(
        Seq(
          googleProvider, facebookProvider, twitterProvider,
          vkProvider, xingProvider, yahooProvider, clefProvider
        )
      )
    }
  }

  /*object SilhouetteAuthInfoRepository {
    def apply(
               passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
               oauth1InfoDAO: DelegableAuthInfoDAO[OAuth1Info],
               oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info],
               openIDInfoDAO: DelegableAuthInfoDAO[OpenIDInfo]
             ): AuthInfoRepository = {
      new DelegableAuthInfoRepository(
        passwordInfoDAO, oauth1InfoDAO, oauth2InfoDAO, openIDInfoDAO
      )
    }
  }*/

  object SilhouetteCredentialsProvider {
    def apply(
               authInfoRepository: AuthInfoRepository,
               passwordHasher: PasswordHasher
             ): CredentialsProvider = {
      new CredentialsProvider(authInfoRepository, passwordHasher))
    }
  }

}
