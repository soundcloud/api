package com.soundcloud.publicApiStrangler.amqp

import com.rabbitmq.client.{ShutdownSignalException, ShutdownListener}
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory

class ShutdownSupervisor extends ShutdownListener {

  private val logger = SoundCloudLoggerFactory.getLogger(getClass)

  def shutdownCompleted(cause: ShutdownSignalException): Unit = {
    logger.error("Connection to RabbitMQ broke down", cause)
  }
}
