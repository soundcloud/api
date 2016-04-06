package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.{AppConfig, ConfigConvention}
import com.soundcloud.jvmkit.zookeeper.{BasePath, ZkClient}
import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.testutilities.{GratisMusikDiebstahl, SpinningUpAppSupport}
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry

class RateLimitingSanitySpecification extends UnitSpecification with SpinningUpAppSupport {

  trait Context extends super.Context {
    val server = TestServer(dockerBasedHost, 5000)
    val adminServer = TestServer(dockerBasedHost, 5001)

    val config = new AppConfig

    val zkClient = {
      val zookeeperServers = config.get(ResourceName("ZOOKEEPER"), ConfigConvention.SERVERS)
      val baseSleepTimeInMilliseconds = 1000
      val maxNumberOfRetries = 5
      val retryPolicy = new ExponentialBackoffRetry(baseSleepTimeInMilliseconds, maxNumberOfRetries)
      val curatorZookeeperClient = CuratorFrameworkFactory.newClient(zookeeperServers, retryPolicy)
      curatorZookeeperClient.start()
      curatorZookeeperClient.blockUntilConnected()
      new ZkClient(curatorZookeeperClient)
    }
  }

  "Public API Strangler" should {

    "rate limit test requests" in new Context {
      private def setData(zkClient: ZkClient, path: String, data: String) = {
        zkClient.createRecursively(BasePath.from(path), data.getBytes)
      }

      setData(zkClient, "/public-api-strangler/rollouts/wire-rate-limits", "100")
      setData(zkClient, "/public-api-strangler/rollouts/probe-rate-limits", "100")
      setData(zkClient, "/public-api-strangler/rollouts/enforce-rate-limits", "100")
      setData(zkClient, "/ratelimiting/public-api-strangler/ratelimitgroups/default",
        """
          |{
          |  "id":"default",
          |  "rate_limits": [
          |    {
          |      "name":"limit-all-the-things",
          |      "matching":".*",
          |      "configurations": [
          |        {
          |          "bucket":"by-client",
          |          "time_window":"PT1M",
          |          "max_nr_of_requests":3
          |        }
          |      ],
          |      "mode":"enforcing"
          |    }
          |  ]
          |}
        """.stripMargin)

      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}").status ==== 200
      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}").status ==== 200
      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}").status ==== 200

      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}").status ==== 429
    }
  }
}
