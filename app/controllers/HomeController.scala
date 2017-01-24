package controllers

import javax.inject._

import com.mohiva.play.silhouette.api.Silhouette
import models.daos.NotificationDAO
import models.{ Account, Notification }
import play.api._
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc._
import utils.authentication.DefaultEnv

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  notificationDao: NotificationDAO
)
  extends Controller with I18nSupport {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  def dashboard = silhouette.SecuredAction.async { implicit request ⇒
    notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
      Future.successful(Ok(views.html.dashboard(request.identity, notifications)))
    }
  }

  def notification = silhouette.SecuredAction.async { implicit request ⇒
    notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
      Future.successful(Ok(views.html.dashboard(request.identity, notifications)))
    }
  }

  //  de f tickets = Action { Ok(views.html.tickets()) }
  //  def ticket = Action { Ok(views.html.ticket()) }
  //  def notification = Action { Ok(views.html.notification()) }
  //  def edit = Action { Ok(views.html.edit()) }
  //  def status = Action { Ok(views.html.status()) }
  //  def dispatch = Action { Ok(views.html.dispatch()) }
  //  def statistic = Action { Ok(views.html.statistic()) }
  //  def build = Action { Ok(views.html.build()) }

}
