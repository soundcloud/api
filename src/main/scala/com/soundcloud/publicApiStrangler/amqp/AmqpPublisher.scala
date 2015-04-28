package com.soundcloud.publicApiStrangler.amqp

import java.util.concurrent.Executors

import com.rabbitmq.client.{ConnectionFactory, MessageProperties}
import com.twitter.io.Charsets
import com.twitter.util.{Future, FuturePool}
import play.api.libs.json.{Json, Writes}

object AmqpPublisher {
  private val ExchangeName = "publicapistrangler"
}

class AmqpPublisher(connectionFactory: ConnectionFactory) {

  def publish[Event : Writes](routingKey: String, event: Event): Future[Unit] = bulkheaded {
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
