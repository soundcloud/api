package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.config.BazookaConfig
import com.soundcloud.publicApiStrangler.zookeeper.ZookeeperClientFactory
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.Await
import org.specs2.specification.AfterEach

class WhitelistingServiceSpec extends UnitSpecification {
  sequential

  "The whitelisting service" should {

    trait Context extends AfterEach {
      val config = new BazookaConfig
      val factory = new ZookeeperClientFactory
      val zookeeperClient = factory.create(config)
      val whitelistingService = new WhitelistingService(zookeeperClient, config.getApplicationName)

      override def after = {
        zookeeperClient.delete().deletingChildrenIfNeeded().forPath(s"/${config.getApplicationName}")
      }
    }

    "whitelist a client" in new Context {
      val client = Urn("soundcloud", "applications", "testclient-1")
      Await.result(whitelistingService.whitelistClient(client))
      whitelistingService.hasClientWhitelisted(client) must beTrue
    }

    "unwhitelist a client" in new Context {
      val client = Urn("soundcloud", "applications", "testclient-1")
      Await.result(whitelistingService.whitelistClient(client))
      Await.result(whitelistingService.unwhitelistClient(client))
      whitelistingService.hasClientWhitelisted(client) must beFalse
    }

    "behave idempotently when whitelisting an already whitelisted client" in new Context {
      val client = Urn("soundcloud", "applications", "testclient-1")
      Await.result(whitelistingService.whitelistClient(client))
      Await.result(whitelistingService.whitelistClient(client))
      whitelistingService.hasClientWhitelisted(client) must beTrue
    }

    "behave idempotently when unwhitelisting a client currently not in the whitelist" in new Context {
      val client = Urn("soundcloud", "applications", "testclient-1")
      Await.result(whitelistingService.unwhitelistClient(client))
      whitelistingService.hasClientWhitelisted(client) must beFalse
    }

    "return all the whitelisted clients" in new Context {
      val client1 = Urn("soundcloud", "applications", "testclient-1")
      val client2 = Urn("soundcloud", "applications", "testclient-2")
      val client3 = Urn("soundcloud", "applications", "testclient-3")
      Await.result(whitelistingService.whitelistClient(client3))
      Await.result(whitelistingService.whitelistClient(client2))
      Await.result(whitelistingService.whitelistClient(client1))
      Await.result(whitelistingService.unwhitelistClient(client2))
      whitelistingService.whitelistedClients.toSeq ==== Seq(client1, client3)
    }
  }
}
