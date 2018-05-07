package com.example.helloextkafka.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.example.helloextkafka.api.HelloExtKafkaService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.softwaremill.macwire._

class HelloExtKafkaLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new HelloExtKafkaApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new HelloExtKafkaApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[HelloExtKafkaService])
}

abstract class HelloExtKafkaApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[HelloExtKafkaService](wire[HelloExtKafkaServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = HelloExtKafkaSerializerRegistry

  // Register the hello-ext-kafka persistent entity
  persistentEntityRegistry.register(wire[HelloExtKafkaEntity])
}
