package controllers

import javax.inject.{ Inject, Named, Singleton }

import akka.actor.ActorRef
import akka.camel.CamelMessage
import com.mohiva.play.silhouette.api.Silhouette
import forms.BuildModelForm
import models.{ BuildSession, ModelConfig, ModelConfigObj }
import models.daos.{ BuildSessionDAO, NotificationDAO }
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import utils.authentication.{ DefaultEnv, WithAdminUserType }

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json

@Singleton
class ModelController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  notificationDao: NotificationDAO,
  buildSessionDAO: BuildSessionDAO,
  @Named("build-model-request") buildModelRequest: ActorRef,
  @Named("build-logger-receiver") buildLogger: ActorRef
) extends Controller with I18nSupport {

  def view = silhouette.SecuredAction(WithAdminUserType()).async { implicit request ⇒
    notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
      var modelConfig = ModelConfig(ModelConfigObj.treshold, ModelConfigObj.enableAutoUpdateModel, ModelConfigObj.intervalAutoUpdate)
      Future.successful(Ok(views.html.build(request.identity, notifications, modelConfig, BuildModelForm.form)))
    }
  }

  def config = silhouette.SecuredAction(WithAdminUserType()).async { implicit request ⇒
    BuildModelForm.form.bindFromRequest.fold(
      form ⇒ Future.successful(Redirect(routes.ModelController.view())),
      data ⇒ Future.successful {
        ModelConfigObj.treshold = data.treshold.toDouble
        ModelConfigObj.enableAutoUpdateModel = data.enableAutoUpdate
        ModelConfigObj.intervalAutoUpdate = data.intervalAutoUpdate

        if (ModelConfigObj.enableAutoUpdateModel) buildLogger ! "Build"
        else buildLogger ! "Stop"

        Redirect(routes.ModelController.view())
      }
    )
  }

  def build = silhouette.SecuredAction(WithAdminUserType()).async { implicit request ⇒
    val newBuildSession = BuildSession(id = None, caller = request.identity.username)
    buildSessionDAO.save(newBuildSession).flatMap { session ⇒
      buildModelRequest ! CamelMessage(session.id.getOrElse(10000), Map(CamelMessage.MessageExchangeId → "ManualBuildModel"))
      Future(Redirect(routes.ModelController.buildSession(session.id.getOrElse(10000))))
    }
  }

  def buildSession(id: Long) = silhouette.SecuredAction(WithAdminUserType()).async { implicit request ⇒
    notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
      buildSessionDAO.find(id).flatMap { maybeSession ⇒
        maybeSession match {
          case None ⇒ Future(Redirect(routes.HomeController.dashboard()))
          case Some(session) ⇒
            if (session.caller == request.identity.username)
              Future(Ok(views.html.buildsession(request.identity, notifications, session)))
            else
              Future(Redirect(routes.HomeController.dashboard()))
        }
      }
    }
  }

  def getLog(id: Long) = silhouette.SecuredAction(WithAdminUserType()).async { implicit request ⇒
    buildSessionDAO.find(id).flatMap { maybeSession ⇒
      maybeSession match {
        case None ⇒ Future(Redirect(routes.HomeController.dashboard()))
        case Some(session) ⇒
          if (session.caller == request.identity.username)
            Future.successful(Ok(Json.obj("log" → session.log)))
          else
            Future(Redirect(routes.HomeController.dashboard()))
      }
    }
  }

}
