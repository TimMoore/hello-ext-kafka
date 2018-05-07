Simple test of running Lagom with an external Kafka broker.

This expects a Kafka broker to be running on localhost:9992.

The specific configuration changes are:

1.  In `build.sbt`:

    ```scala
    lagomKafkaEnabled in ThisBuild := false
    lagomKafkaAddress in ThisBuild := "localhost:9992"
    ```
2.  In `application.conf`:

    ```
    # Use an external Kafka broker running on port 9992
    lagom.broker.kafka.service-name = ""
    #lagom.broker.kafka.brokers = "lagom:9092"
    lagom.broker.kafka.brokers = "localhost:9992"
    ```
3.  In `HelloExtKafkaServiceSpec.scala`:

    ```scala
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
    ```
4.  A `logback.xml` configuration has been provided with Kafka set to
    log at `INFO` level.
