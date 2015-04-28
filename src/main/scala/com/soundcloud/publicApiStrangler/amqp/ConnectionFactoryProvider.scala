package com.soundcloud.publicApiStrangler.amqp

import com.rabbitmq.client.ConnectionFactory
import com.soundcloud.jvmkit.config.Config

class ConnectionFactoryProvider(config: Config) {

  lazy val get = {
    val factory = new ConnectionFactory()
    factory.setUri(config.get("RABBITMQ_URI"))
    factory.setRequestedHeartbeat(config.get("RABBITMQ_HEARTBEAT_IN_SECONDS").toInt)
    factory.setConnectionTimeout(config.get("RABBITMQ_CONNECTION_TIMEOUT_IN_MILLISECONDS").toInt)
    factory.setNetworkRecoveryInterval(config.get("RABBITMQ_NETWORK_RECOVERY_INTERVAL_IN_MILLISECONDS").toLong)
    factory.setAutomaticRecoveryEnabled(true)
    factory.setTopologyRecoveryEnabled(true)
    factory
  }

}
