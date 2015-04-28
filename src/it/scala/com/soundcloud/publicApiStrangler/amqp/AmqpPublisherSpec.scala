package com.soundcloud.publicApiStrangler.amqp

import com.rabbitmq.client._
import com.soundcloud.jvmkit.config.BazookaConfig
import com.soundcloud.publicApiStrangler.amqp.AmqpPublisherSpec.ConsumerProbe
import com.twitter.common.util.Timer
import com.twitter.io.{Charsets, Buf}
import com.twitter.util.TimeConversions._
import com.twitter.util._
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import org.specs2.time.NoTimeConversions
import play.api.libs.json.Json

class AmqpPublisherSpec extends Specification with NoTimeConversions {

  "AmqpPublisher" should {

    trait Context extends Scope {
      val config = new BazookaConfig
      val connectionFactoryProvider = new ConnectionFactoryProvider(config)
      val publisher = new AmqpPublisher(connectionFactoryProvider.get)
      val probe = new ConsumerProbe(connectionFactoryProvider.get)
    }

    "publish an event that should eventually be received" in new Context {
      val res = publisher.publish("someRoutingKey", Json.obj("event" -> "some_type"))
      val message = Await.result(probe.receive(5.seconds))
      message.envelope.getRoutingKey ==== "someRoutingKey"
      message.envelope.getExchange ==== "publicapistrangler"
      Json.parse(message.body) ==== Json.obj("event" -> "some_type")
    }

  }
}

object AmqpPublisherSpec {

  private class ConsumerProbe(connectionFactory: ConnectionFactory) {
    private[this] val connection = connectionFactory.newConnection()
    private[this] val channel = connection.createChannel()
    channel.exchangeDeclare("publicapistrangler", "direct", true)
    private[this] val queueName = channel.queueDeclare().getQueue
    channel.queueBind(queueName, "publicapistrangler", "someRoutingKey")

    def receive(timeout: Duration): Future[Message] = {
      val promise = Promise[Message]()
      channel.basicConsume(queueName, true, "myConsumerTag", new ProbingConsumer(channel, promise))
      promise.within(new JavaTimer(), timeout)
    }

  }

  case class Message(envelope: Envelope, properties: BasicProperties, body: String)

  private class ProbingConsumer(channel: Channel, promise: Promise[Message]) extends DefaultConsumer(channel) {
    override def handleDelivery(
        consumerTag: String,
        envelope: Envelope,
        properties: AMQP.BasicProperties,
        body: Array[Byte]) = {
      val message = Message(envelope, properties, new String(body, Charsets.Utf8))
      promise.setValue(message)
    }
  }

}
