package utils

import javax.inject.{ Inject, Singleton }

import akka.actor.ActorSystem
import akka.camel.CamelExtension
import org.apache.activemq.camel.component.ActiveMQComponent

@Singleton
class CamelContextInitiator @Inject() (system: ActorSystem) {

  val camel = CamelExtension(system)
  val amqUrl = "nio://localhost:61616"
  camel.context.addComponent("activemq", ActiveMQComponent.activeMQComponent("nio://localhost:61616"))
  System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "*")

}
