package actors

import java.time.LocalTime

import javax.inject.{ Inject, Singleton }

import akka.actor.{ Actor, Props }
import akka.actor.Actor.Receive
import akka.camel.{ CamelMessage, Consumer }
import models.{ Notification, Ticket }
import models.daos.NotificationDAO

object NotificationManager {
  def props = Props[NotificationManager]
}

@Singleton
class NotificationManager @Inject() (
  notificationDao: NotificationDAO
) extends Actor with Consumer {

  override def endpointUri: String = "activemq:topic:New.Ticket"

  override def receive: Receive = {
    case event: CamelMessage if event.headers(CamelMessage.MessageExchangeId) == "NewTicketStored" ⇒ {
      println("\n" + LocalTime.now)
      println("[Notification Manager] Received Event Ticket from Ticket Management")
      val ticket = event.bodyAs[Ticket]
      val notification = Notification(
        id = None,
        belongsTo = ticket.assignee.getOrElse(""),
        isViewed = false,
        origin = Some("New Ticket"),
        content = Some("New Ticket Received -- " + ticket.description.getOrElse(""))
      )
      notificationDao.save(notification)
    }

    case event: CamelMessage if event.headers(CamelMessage.MessageExchangeId) == "ManuallyDispatchedTicket" ⇒ {
      println("\n" + LocalTime.now)
      println("[Notification Manager] Received Event Ticket from Ticket Management")
      val ticket = event.bodyAs[Ticket]
      val notification = Notification(
        id = None,
        belongsTo = ticket.assignee.getOrElse(""),
        isViewed = false,
        origin = Some("Disposisi Manual"),
        content = Some("Manually Dispatched Ticket -- " + ticket.description.getOrElse(""))
      )
      notificationDao.save(notification)
    }
  }

}