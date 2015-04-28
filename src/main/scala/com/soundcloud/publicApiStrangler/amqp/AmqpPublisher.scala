package com.soundcloud.publicApiStrangler.amqp

import java.util.concurrent.Executors

import com.rabbitmq.client.{ConnectionFactory, MessageProperties}
import com.soundcloud.publicApiStrangler.rateLimiting.semanticevents.{SemanticEventPayload, SemanticEvent}
import com.twitter.io.Charsets
import com.twitter.util.{Future, FuturePool}
import play.api.libs.json.{Writes, Json}

object AmqpPublisher {
  private val ExchangeName = "publicapistrangler"
}

class AmqpPublisher(connectionFactory: ConnectionFactory) {

  def publish[E <: SemanticEventPayload : Writes](routingKey: String, event: SemanticEvent[E]): Future[Unit] = bulkheaded {
    val json = Json.toJson(event)
    val payload = Json.stringify(json).getBytes(Charsets.Utf8)
    channel.basicPublish(AmqpPublisher.ExchangeName, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, payload)
  }

  private val connection = {
    val conn = connectionFactory.newConnection()
    conn.addShutdownListener(new ShutdownSupervisor)
    conn
  }

  private val channel = {
    val c = connection.createChannel()
    c.exchangeDeclare(AmqpPublisher.ExchangeName, "direct", true)
    c
  }

  private val bulkheaded = FuturePool(Executors.newFixedThreadPool(8))
}
