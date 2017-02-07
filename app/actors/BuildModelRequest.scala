package actors

import javax.inject.{ Inject, Named, Singleton }

import akka.actor.{ Actor, ActorRef, Cancellable, Props }
import akka.camel.{ CamelMessage, Consumer, Oneway, Producer }
import models.{ BuildLog, ModelConfigObj }
import models.daos.BuildSessionDAO

import scala.concurrent.duration._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import scala.language.postfixOps

object BuildModelRequest {
  def props = Props[BuildModelRequest]
}

@Singleton
class BuildModelRequest extends Actor with Producer with Oneway {

  override def endpointUri: String = "activemq:topic:Build.Model"

}

object BuildLoggerReceiver {
  def props = Props[BuildLoggerReceiver]
}

@Singleton
class BuildLoggerReceiver @Inject() (
  @Named("build-model-request") buildModelRequest: ActorRef,
  buildSessionDAO: BuildSessionDAO
) extends Actor with Consumer {

  import context.dispatcher

  val Build = "Build"
  val Stop = "Stop"

  var autoBuild: Cancellable = _;

  override def endpointUri: String = "activemq:topic:Build.Log"

  override def receive: Receive = {
    case event: CamelMessage if event.headers(CamelMessage.MessageExchangeId) == "BuildLog" ⇒ {
      val message = event.bodyAs[BuildLog]
      buildSessionDAO.find(message.id).flatMap { session ⇒
        session match {
          case None ⇒ Future {}
          case Some(session) ⇒
            val newLog = session.log + "<br>" + message.log
            buildSessionDAO.save(session.copy(log = newLog))
        }
      }
    }

    case Build ⇒ autoBuild = context.system.scheduler.schedule(
      0 milliseconds,
      ModelConfigObj.intervalAutoUpdate seconds,
      buildModelRequest,
      CamelMessage("", Map(CamelMessage.MessageExchangeId → Build)))

    case Stop ⇒ autoBuild.cancel()

  }

}