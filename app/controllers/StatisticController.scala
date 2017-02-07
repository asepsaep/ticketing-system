package controllers

import javax.inject.{ Inject, Singleton }

import com.mohiva.play.silhouette.api.Silhouette
import forms.{ DispatchTicketForm, DummyForm }
import models.daos.{ AccountDAO, NotificationDAO, TicketDAO }
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import play.api.libs.json._
import play.api.libs.functional.syntax._
import utils.authentication.DefaultEnv

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

@Singleton
class StatisticController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  notificationDao: NotificationDAO,
  ticketDao: TicketDAO,
  accountDao: AccountDAO
) extends Controller with I18nSupport {

  implicit val locationWrites: Writes[TicketStatusStatistic] = (
    (JsPath \ "label").write[String] and
    (JsPath \ "value").write[Int] and
    (JsPath \ "color").write[String] and
    (JsPath \ "highlight").write[String]
  )(unlift(TicketStatusStatistic.unapply))

  def view = silhouette.SecuredAction.async { implicit request ⇒
    notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
      accountDao.findByUserType("user").flatMap { users ⇒
        val listUsers = users.map(_.username).zip(users.map(_.name.getOrElse("")))
        Future.successful(Ok(views.html.statistic(request.identity, notifications, DummyForm.form, Seq(("admin", "Admin")) ++ listUsers)))
      }
    }
  }

  def stat(username: String = "admin") = silhouette.UserAwareAction.async {
    val ticketsCall = if (username == "admin") ticketDao.all else ticketDao.findTo(username)

    ticketsCall.flatMap { tickets ⇒
      val stats = Seq(
        TicketStatusStatistic("New", tickets.count(_.status == Some("New")), "#f56954", "#f56954"),
        TicketStatusStatistic("Assigned", tickets.count(_.status == Some("Assigned")), "#00c0ef", "#00c0ef"),
        TicketStatusStatistic("Closed", tickets.count(_.status == Some("Closed")), "#00a65a", "#00a65a")
      )
      Future.successful(Ok(Json.toJson(stats)))
    }
  }

  def stat2(username: String = "admin") = silhouette.UserAwareAction.async {
    val ticketsCall = if (username == "admin") ticketDao.all else ticketDao.findTo(username)

    ticketsCall.flatMap { tickets ⇒
      val stats = Seq(
        TicketStatusStatistic("Critical", tickets.count(_.priority == Some("Critical")), "#f56954", "#f56954"),
        TicketStatusStatistic("High", tickets.count(_.priority == Some("High")), "#f39c12", "#f39c12"),
        TicketStatusStatistic("Medium", tickets.count(_.priority == Some("Medium")), "#00a65a", "#00a65a"),
        TicketStatusStatistic("Low", tickets.count(_.priority == Some("Low")), "#3c8dbc", "#3c8dbc")
      )
      Future.successful(Ok(Json.toJson(stats)))
    }
  }

}

case class TicketStatusStatistic(
  label: String,
  value: Int,
  color: String,
  highlight: String
)