package controllers

import javax.inject.{ Inject, Singleton }

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.LoginForm
import models.services.AccountService
import play.api.Configuration
import play.api.data.Form
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.mvc.{ AnyContent, Controller, Request }
import utils.authentication.DefaultEnv
import net.ceedubs.ficus.Ficus._

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.duration.FiniteDuration

@Singleton
class LoginController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  accountService: AccountService,
  authInfoRepository: AuthInfoRepository,
  credentialsProvider: CredentialsProvider,
  configuration: Configuration,
  clock: Clock
) extends Controller with I18nSupport {

  def view = silhouette.UnsecuredAction.async { implicit request ⇒
    Future.successful(Ok(views.html.login(LoginForm.form)))
  }

  def submit = silhouette.UnsecuredAction.async { implicit request ⇒
    formFoldHelper(LoginForm.form.bindFromRequest)
  }

  def formFoldHelper(form: Form[LoginForm.Data])(implicit request: Request[AnyContent]) = {
    form.fold(
      form ⇒ Future.successful(BadRequest(views.html.login(form))),
      data ⇒ {
        val credentials = Credentials(data.username, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo ⇒
          val result = Redirect(routes.HomeController.dashboard())
          accountService.retrieve(loginInfo).flatMap {
            case Some(account) ⇒ {
              val c = configuration.underlying
              silhouette.env.authenticatorService.create(loginInfo).map {
                case authenticator if data.rememberMe ⇒
                  authenticator.copy(
                    expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                    idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout"),
                    cookieMaxAge = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.cookieMaxAge")
                  )
                case authenticator ⇒ authenticator
              }.flatMap { authenticator ⇒
                silhouette.env.authenticatorService.init(authenticator).flatMap { v ⇒
                  silhouette.env.authenticatorService.embed(v, result)
                }
              }
            }
            case None ⇒ {
              Future.failed(new IdentityNotFoundException("Couldn't find user"))
            }
          }
        }.recover {
          case e: ProviderException ⇒
            Redirect(routes.LoginController.view()).flashing("error" → "Invalid credential")
        }
      }
    )
  }

}
