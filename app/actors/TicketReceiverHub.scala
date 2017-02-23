package actors

import javax.inject.Singleton

import akka.actor.Props
import akka.camel.{ CamelMessage, Oneway, Producer }

object TicketReceiverHub {
  def props = Props[TicketReceiverHub]
}

@Singleton
class TicketReceiverHub extends Producer with Oneway {

  override def endpointUri: String = "activemq:topic:New.Ticket"

}