package controllers

import javax.inject.{ Inject, Singleton }

import com.mohiva.play.silhouette.api.{ LoginInfo, Silhouette }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.RegisterForm
import models.Account
import models.services.AccountService
import play.api.data.Form
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.mvc.{ AnyContent, Controller, Request }
import utils.authentication.DefaultEnv

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

@Singleton
class RegisterController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  accountService: AccountService,
  authInfoRepository: AuthInfoRepository,
  passwordHasher: PasswordHasher
) extends Controller with I18nSupport {

  def view = silhouette.UnsecuredAction.async { implicit request ⇒
    Future.successful(Ok(views.html.register(RegisterForm.form)))
  }

  def submit = silhouette.UnsecuredAction.async { implicit request ⇒
    formFoldHelper(RegisterForm.form.bindFromRequest)
  }

  protected def formFoldHelper(form: Form[RegisterForm.Data])(implicit request: Request[AnyContent]) = {
    form.fold(
      form ⇒ Future.successful(BadRequest(views.html.register(form))),
      data ⇒ {
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.username)
        accountService.retrieveAlongWithEmail(loginInfo, data.email).flatMap {
          case Some(account) ⇒
            Future.successful(Redirect(routes.RegisterController.view()).flashing("error" → "User already exists"))
          case None ⇒
            val authInfo = passwordHasher.hash(data.password)
            val account = Account(
              id = None,
              providerId = loginInfo.providerID,
              providerKey = loginInfo.providerKey,
              username = data.username,
              name = Some(data.name),
              email = Some(data.email),
              userType = Some("user"),
              avatarUrl = Some("profile.jpg")
            )
            for {
              account ← accountService.save(account)
              authInfo ← authInfoRepository.add(loginInfo, authInfo)
              authenticator ← silhouette.env.authenticatorService.create(loginInfo)
              value ← silhouette.env.authenticatorService.init(authenticator)
              result ← silhouette.env.authenticatorService.embed(value, Redirect(routes.HomeController.dashboard))
            } yield {
              result
            }
        }
      }
    )
  }

}
