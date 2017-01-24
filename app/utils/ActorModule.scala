package utils

import actors.{ NotificationManager, TicketReceiver, TicketReceiverHub }
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class ActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    //    bindActor[TicketReceiver]("ticket-receiver")
    //    bindActor[TicketReceiverHub]("ticket-receiver-hub")
    //    bindActor[NotificationManager]("notification-manager")
    //    bindActor[BatchTrainer]("batch-trainer")
    //    bindActor[Classifier]("classifier")
    //    bindActor[Director]("director")
    //    bindActor[CorpusInitializer]("corpus-initializer")
    //    bindActor[SimilarTicketFinder]("similar-ticket-finder")
  }

}
