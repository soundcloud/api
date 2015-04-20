package com.soundcloud.publicApiStrangler.zookeeper

import com.soundcloud.jvmkit.config.Config
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry

class ZookeeperClientFactory {

  private val baseSleepTimeInMiliseconds = 1000
  private val maxNumberOfRetries = 5

  val retryPolicy = new ExponentialBackoffRetry(baseSleepTimeInMiliseconds, maxNumberOfRetries)

  def create(config: Config): CuratorFramework = {
    val zookeeperServers = config.get("ZOOKEEPER_SERVERS")
    val curatorZookeeperClient = CuratorFrameworkFactory.newClient(zookeeperServers, retryPolicy)

    curatorZookeeperClient.start()
    curatorZookeeperClient.blockUntilConnected()

    curatorZookeeperClient
  }
}
