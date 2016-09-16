package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.config.AppConfig
import com.soundcloud.jvmkit.zookeeper.{BasePath, ZkClient}
import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.testutilities.{GratisMusikDiebstahl, SpinningUpAppSupport}
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry

class RateLimitingSanitySpecification extends UnitSpecification with SpinningUpAppSupport {

  trait Context extends super.Context {
    val server = TestServer("strangler", 5000)
    val adminServer = TestServer("strangler", 5001)

    val config = new AppConfig

    val zkClient = {
      val zookeeperServers = "zookeeper:2181"
      val baseSleepTimeInMilliseconds = 1000
      val maxNumberOfRetries = 5
      val retryPolicy = new ExponentialBackoffRetry(baseSleepTimeInMilliseconds, maxNumberOfRetries)
      val curatorZookeeperClient = CuratorFrameworkFactory.newClient(zookeeperServers, retryPolicy)
      curatorZookeeperClient.start()
      curatorZookeeperClient.blockUntilConnected()
      new ZkClient(curatorZookeeperClient)
    }

    private[RateLimitingSanitySpecification] def setData(zkClient: ZkClient, path: String, data: String) = {
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
        |      "name":"limit-foo",
        |      "matching":"/foo",
        |      "configurations": [
        |        {
        |          "bucket":"by-client",
        |          "time_window":"PT1M",
        |          "max_nr_of_requests":1000
        |        }
        |      ],
        |      "mode":"probing"
        |    },
        |    {
        |      "name":"search",
        |      "matching":"",
        |      "configurations": [
        |        {
        |          "bucket":"by-client",
        |          "time_window":"PT1M",
        |          "max_nr_of_requests":3
        |        }
        |      ],
        |      "mode":"enforcing"
        |    },
        |    {
        |      "name": "plays",
        |      "matching": "",
        |      "configurations": [
        |          {
        |              "bucket": "by-client",
        |              "time_window": "PT1M",
        |              "max_nr_of_requests": 3
        |          }
        |      ],
        |      "mode": "enforcing"
        |    }
        |  ]
        |}
      """.stripMargin)
  }

  "Public API Strangler" should {

    "rate limit test requests satisfying custom classifier, with search query params" in new Context {

      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}&q=search-foo").status ==== 200
      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}&license=search-bar").status ==== 200
      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}&tags=search-baz").status ==== 200

      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}&q=search-foo").status ==== 429
      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}&license=search-foo").status ==== 429
      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}&tags=search-foo").status ==== 429
      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}&genres=search-foo").status ==== 429

      server.get(s"/search?q=foo&client_id=${GratisMusikDiebstahl.clientId}").status ==== 429
      server.get(s"/v1/tracks?q=foo&client_id=${GratisMusikDiebstahl.clientId}").status ==== 429

      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}&foo=search-foo").status ==== 200
      server.get(s"/tracks/13158665.json?client_id=${GratisMusikDiebstahl.clientId}").status ==== 200
    }

    "ratelimit requests for track streams injected via config in code, and not ZK" in new Context {
      server.get(s"/i1/tracks/177748926/streams?client_id=6320d5b73121db59e21259fc688d940c").status ==== 200
      server.get(s"/i1/tracks/177748926/streams?client_id=6320d5b73121db59e21259fc688d940c").status ==== 200
      server.get(s"/i1/tracks/177748926/streams?client_id=6320d5b73121db59e21259fc688d940c").status ==== 200

      server.get(s"/i1/tracks/177748926/streams?client_id=6320d5b73121db59e21259fc688d940c").status ==== 429

    }
  }
}
