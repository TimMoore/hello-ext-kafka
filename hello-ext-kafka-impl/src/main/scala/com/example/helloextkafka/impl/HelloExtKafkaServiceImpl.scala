package com.example.helloextkafka.impl

import com.example.helloextkafka.api
import com.example.helloextkafka.api.{HelloExtKafkaService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

/**
  * Implementation of the HelloExtKafkaService.
  */
class HelloExtKafkaServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends HelloExtKafkaService {

  override def hello(id: String) = ServiceCall { _ =>
    // Look up the hello-ext-kafka entity for the given ID.
    val ref = persistentEntityRegistry.refFor[HelloExtKafkaEntity](id)

    // Ask the entity the Hello command.
    ref.ask(Hello(id))
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // Look up the hello-ext-kafka entity for the given ID.
    val ref = persistentEntityRegistry.refFor[HelloExtKafkaEntity](id)

    // Tell the entity to use the greeting message specified.
    ref.ask(UseGreetingMessage(request.message))
  }


  override def greetingsTopic(): Topic[api.GreetingMessageChanged] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(HelloExtKafkaEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(helloEvent: EventStreamElement[HelloExtKafkaEvent]): api.GreetingMessageChanged = {
    helloEvent.event match {
      case GreetingMessageChanged(msg) => api.GreetingMessageChanged(helloEvent.entityId, msg)
    }
  }
}
