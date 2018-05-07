package com.example.helloextkafka.impl

import akka.stream.scaladsl.Sink
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, Matchers }
import com.example.helloextkafka.api._

class HelloExtKafkaServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new HelloExtKafkaApplication(ctx) with LocalServiceLocator
  }

  import server.materializer

  val client: HelloExtKafkaService = server.serviceClient.implement[HelloExtKafkaService]

  override protected def afterAll() = server.stop()

  "hello-ext-kafka service" should {

    "say hello" in {
      client.hello("Alice").invoke().map { answer =>
        answer should ===("Hello, Alice!")
      }
    }

    "allow responding with a custom message" in {
      for {
        _ <- client.useGreeting("Bob").invoke(GreetingMessage("Hi"))
        answer <- client.hello("Bob").invoke()
      } yield {
        answer should ===("Hi, Bob!")
      }
    }

    "publish custom messages" in {
      for {
        _ <- client.useGreeting("Carol").invoke(GreetingMessage("Howdy"))
        event <- client.greetingsTopic().subscribe.atMostOnceSource
            .filter(_.name == "Carol")
            .runWith(Sink.head)
      } yield {
        event.message shouldBe "Howdy"
      }
    }
  }
}
