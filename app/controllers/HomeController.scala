package controllers

import javax.inject._

import com.mohiva.play.silhouette.api.Silhouette
import models.daos.{ NotificationDAO, TicketDAO }
import models.{ Account, Notification }
import play.api._
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc._
import utils.authentication.DefaultEnv

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  notificationDao: NotificationDAO,
  ticketDao: TicketDAO
)
  extends Controller with I18nSupport {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  //  def dashboard = silhouette.SecuredAction.async { implicit request ⇒
  //    notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
  //      Future.successful(Ok(views.html.dashboard(request.identity, notifications)))
  //    }
  //  }

  def dashboard = silhouette.SecuredAction.async { implicit request ⇒
    notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
      request.identity.userType.getOrElse("user") match {
        case "admin" ⇒ ticketDao.all.flatMap { tickets ⇒ Future.successful(Ok(views.html.tickets(request.identity, notifications, tickets))) }
        case "user"  ⇒ ticketDao.findTo(request.identity.username).flatMap { tickets ⇒ Future.successful(Ok(views.html.tickets(request.identity, notifications, tickets))) }
      }
    }
  }

  def notification = silhouette.SecuredAction.async { implicit request ⇒
    notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
      val markAsRead = notifications.filter(notif ⇒ !notif.isViewed).map(notif ⇒ notif.copy(isViewed = true))
      markAsRead.foreach(notificationDao.save)
      Future.successful(Ok(views.html.notification(request.identity, notifications)))
    }
  }

  def notificationNumber = silhouette.SecuredAction.async { implicit request ⇒
    notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
      val number = notifications.count(notif ⇒ !notif.isViewed)
      val message = if (number == 0) "<i class='fa fa-bell-o'></i>" else "<i class='fa fa-bell-o'></i><strong>" + number + "</strong>"
      val content = notifications.take(10).map(_.content.getOrElse("")).map(notif ⇒ "<li><a href='#'>" + notif + "</a></li>").mkString
      Future.successful(Ok(Json.obj("number" → message, "notification" → content)))
    }
  }

  //  TODO
  //  def statistic = Action { Ok(views.html.statistic()) }
  //  def build = Action { Ok(views.html.build()) }

}
