package actors

import java.time.{ LocalTime, OffsetDateTime }
import javax.inject.{ Inject, Named, Singleton }

import akka.actor.{ Actor, ActorRef, Props }
import akka.camel.{ CamelMessage, Consumer, Oneway, Producer }
import models.{ ModelConfigObj, Ticket, TicketProbability }
import models.daos.{ AccountDAO, TicketDAO }

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

object TicketReceiver {
  def props = Props[TicketReceiver]
}

@Singleton
class TicketReceiver @Inject() (
  accountDao: AccountDAO,
  ticketDao: TicketDAO,
  @Named("ticket-receiver-hub") ticketReceiverHub: ActorRef
) extends Actor with Consumer {

  override def endpointUri: String = "activemq:topic:Ticket.Receiver"

  override def receive: Receive = {
    case event: CamelMessage if event.headers(CamelMessage.MessageExchangeId) == "NewTicketWithProbability" ⇒ {
      println("\n" + LocalTime.now)
      println("[Ticket Management] Received Event Ticket from Classifier")
      val ticket = event.bodyAs[TicketProbability]
      println(ticket)
      accountDao.find(ticket.ticket.assignee.getOrElse("")).flatMap { account ⇒
        val name = if (ticket.probability > (ModelConfigObj.treshold - 0.01)) account.flatMap(_.name) else Option("admin")
        ticketDao.save(ticket.ticket.copy(assigneeName = name, status = Some("New"), priority = Some("High"))).flatMap { storedTicket ⇒
          Future {
            println("[Event Ticket] Ticket Management -> Notification Manager")
            ticketReceiverHub ! CamelMessage(storedTicket, Map(CamelMessage.MessageExchangeId → "ManuallyDispatchedTicket"))
          }
        }
      }
    }

    case event: CamelMessage if event.headers(CamelMessage.MessageExchangeId) == "NewTicket" ⇒ {
      println("\n" + LocalTime.now)
      println("[Ticket Management] Received Event Ticket")
      val ticket = event.bodyAs[Ticket]
      println(ticket)
      ticketDao.save(ticket.copy(assignee = Some("admin"), status = Some("New"), priority = Some("High"))).flatMap { storedTicket ⇒
        Future {
          println("[Event Ticket] Ticket Management -> Notification Manager")
          ticketReceiverHub ! CamelMessage(storedTicket, Map(CamelMessage.MessageExchangeId → "NewTicketStored"))
        }
      }
    }

  }

}