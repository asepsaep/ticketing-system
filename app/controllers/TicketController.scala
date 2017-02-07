package controllers

import javax.inject.{ Inject, Named, Singleton }

import akka.actor.ActorRef
import akka.camel.CamelMessage
import com.mohiva.play.silhouette.api.Silhouette
import com.typesafe.config.ConfigFactory
import forms.{ DispatchTicketForm, EditTicketForm, LoginForm, UpdateTicketStatusForm }
import models.daos.{ AccountDAO, NotificationDAO, TicketDAO }
import play.api.data.Form
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.{ AnyContent, Controller, Request }
import utils.authentication.DefaultEnv

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

@Singleton
class TicketController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  notificationDao: NotificationDAO,
  ticketDao: TicketDAO,
  accountDao: AccountDAO,
  @Named("ticket-receiver-hub") ticketReceiverHub: ActorRef
) extends Controller with I18nSupport {

  def tickets = silhouette.SecuredAction.async { implicit request ⇒
    notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
      request.identity.userType.getOrElse("user") match {
        case "admin" ⇒ ticketDao.all.flatMap { tickets ⇒ Future.successful(Ok(views.html.tickets(request.identity, notifications, tickets))) }
        case "user"  ⇒ ticketDao.findTo(request.identity.username).flatMap { tickets ⇒ Future.successful(Ok(views.html.tickets(request.identity, notifications, tickets))) }
      }
    }
  }

  def ticket(id: Long) = silhouette.SecuredAction.async { implicit request ⇒
    notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
      ticketDao.find(id).flatMap {
        case None ⇒ Future.successful(NotFound(views.html.dashboard(request.identity, notifications)))
        case Some(ticket) ⇒ if (request.identity.userType.getOrElse("") == "admin" || request.identity.username == ticket.assignee.getOrElse("")) Future.successful(Ok(views.html.ticket(request.identity, notifications, ticket)))
        else Future.successful(Forbidden(views.html.dashboard(request.identity, notifications)))
      }
    }
  }

  def status(id: Long) = silhouette.SecuredAction.async { implicit request ⇒
    notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
      ticketDao.find(id).flatMap {
        case None ⇒ Future.successful(NotFound(views.html.dashboard(request.identity, notifications)))
        case Some(ticket) ⇒
          import net.ceedubs.ficus.Ficus._
          val config = ConfigFactory.load()
          val status = config.as[Seq[String]]("ticket.status")
          if (request.identity.userType.getOrElse("") == "admin" || request.identity.username == ticket.assignee.getOrElse("")) Future.successful(Ok(views.html.status(request.identity, notifications, ticket, UpdateTicketStatusForm.form, status.zip(status))))
          else Future.successful(Forbidden(views.html.dashboard(request.identity, notifications)))
      }
    }
  }

  def updateStatus = silhouette.SecuredAction.async { implicit request ⇒
    import net.ceedubs.ficus.Ficus._
    val config = ConfigFactory.load()
    val status = config.as[Seq[String]]("ticket.status")

    UpdateTicketStatusForm.form.bindFromRequest.fold(
      form ⇒ notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
        ticketDao.find(form.value.get.id).flatMap { ticket ⇒
          Future.successful(BadRequest(views.html.status(request.identity, notifications, ticket.get, form, status.zip(status))))
        }
      },
      data ⇒ for {
        maybeTicket ← ticketDao.find(data.id)
        ticket ← Future { maybeTicket.get }
        updatedTicket ← ticketDao.save(ticket.copy(status = Some(data.status), resolution = Some(data.resolution)))
      } yield {
        if (updatedTicket.status == Some("Closed") && updatedTicket.assignee != Some("admin")) {
          ticketDao.addAsDataTraining(updatedTicket)
        }
        Redirect(routes.TicketController.ticket(updatedTicket.id.getOrElse(10000)))
      }
    )
  }

  def dispatch(id: Long) = silhouette.SecuredAction.async { implicit request ⇒
    notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
      ticketDao.find(id).flatMap {
        case None ⇒ Future.successful(NotFound(views.html.dashboard(request.identity, notifications)))
        case Some(ticket) ⇒ if (request.identity.userType.getOrElse("") == "admin" || request.identity.username == ticket.assignee.getOrElse("")) {
          accountDao.findByUserType("user").flatMap { users ⇒
            val listUsers = users.map(_.username).zip(users.map(_.name.getOrElse("")))
            Future.successful(Ok(views.html.dispatch(request.identity, notifications, ticket, DispatchTicketForm.form, listUsers)))
          }
        } else Future.successful(Forbidden(views.html.dashboard(request.identity, notifications)))
      }
    }
  }

  def dispatchTicket = silhouette.SecuredAction.async { implicit request ⇒
    accountDao.findByUserType("user").flatMap { users ⇒
      val listUsers = users.map(_.username).zip(users.map(_.name.getOrElse("")))
      DispatchTicketForm.form.bindFromRequest.fold(
        form ⇒ notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
          ticketDao.find(form.value.get.id).flatMap { ticket ⇒
            Future.successful(BadRequest(views.html.dispatch(request.identity, notifications, ticket.get, form, listUsers)))
          }
        },
        data ⇒ for {
          maybeTicket ← ticketDao.find(data.id)
          ticket ← Future { maybeTicket.get }
          maybeUser ← accountDao.find(data.assignee)
          user ← Future { maybeUser.get }
          updatedTicket ← ticketDao.save(ticket.copy(assignee = Option(user.username), assigneeName = user.name))
        } yield {
          ticketReceiverHub ! CamelMessage(updatedTicket, Map(CamelMessage.MessageExchangeId → "ManuallyDispatchedTicket"))
          Redirect(routes.HomeController.dashboard())
        }
      )
    }
  }

  def edit(id: Long) = silhouette.SecuredAction.async { implicit request ⇒
    notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
      ticketDao.find(id).flatMap {
        case None ⇒ Future.successful(NotFound(views.html.dashboard(request.identity, notifications)))
        case Some(ticket) ⇒
          import net.ceedubs.ficus.Ficus._
          val config = ConfigFactory.load()
          val priority = config.as[Seq[String]]("ticket.priority")
          if (request.identity.userType.getOrElse("") == "admin" || request.identity.username == ticket.assignee.getOrElse("")) Future.successful(Ok(views.html.edit(request.identity, notifications, ticket, EditTicketForm.form, priority.zip(priority))))
          else Future.successful(Forbidden(views.html.dashboard(request.identity, notifications)))
      }
    }
  }

  def editTicket = silhouette.SecuredAction.async { implicit request ⇒
    import net.ceedubs.ficus.Ficus._
    val config = ConfigFactory.load()
    val priority = config.as[Seq[String]]("ticket.priority")

    EditTicketForm.form.bindFromRequest.fold(
      form ⇒ notificationDao.findFrom(request.identity.username).flatMap { notifications ⇒
        ticketDao.find(form.value.get.id).flatMap { ticket ⇒
          Future.successful(BadRequest(views.html.edit(request.identity, notifications, ticket.get, form, priority.zip(priority))))
        }
      },
      data ⇒ for {
        maybeTicket ← ticketDao.find(data.id)
        ticket ← Future { maybeTicket.get }
        updatedTicket ← ticketDao.save(ticket.copy(title = Some(data.title), description = Some(data.description), priority = Some(data.priority)))
      } yield {
        Redirect(routes.TicketController.ticket(updatedTicket.id.getOrElse(10000)))
      }
    )
  }

}
