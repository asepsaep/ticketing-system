package controllers

import javax.inject.{ Inject, Singleton }

import com.mohiva.play.silhouette.api.Silhouette
import forms.BuildModelForm
import models.ModelConfig
import models.daos.NotificationDAO
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import utils.authentication.{ DefaultEnv, WithAdminUserType }

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

@Singleton
class ModelController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  notificationDao: NotificationDAO
) extends Controller with I18nSupport {

  protected var modelConfig = ModelConfig()

  def view = silhouette.SecuredAction(WithAdminUserType()).async { implicit request ⇒
    notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
      Future.successful(Ok(views.html.build(request.identity, notifications, modelConfig, BuildModelForm.form)))
    }
  }

  def build = silhouette.SecuredAction(WithAdminUserType()).async { implicit request ⇒
    BuildModelForm.form.bindFromRequest.fold(
      form ⇒ Future.successful(Redirect(routes.ModelController.view())),
      data ⇒ Future.successful {
        modelConfig = ModelConfig(data.treshold.toDouble, data.enableAutoUpdate, data.intervalAutoUpdate)
        Redirect(routes.ModelController.view())
      }
    )
  }

}
