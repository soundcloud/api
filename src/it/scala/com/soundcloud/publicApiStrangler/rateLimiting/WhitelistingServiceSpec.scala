package com.soundcloud.publicApiStrangler.rateLimiting

import java.util.TimerTask
import java.util.concurrent.Executors

import com.soundcloud.jvmkit.config.BazookaConfig
import com.soundcloud.publicApiStrangler.zookeeper.ZookeeperClientFactory
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.{Duration, Await}
import org.specs2.matcher.MatchResult
import org.specs2.specification.AfterEach
import org.specs2.time.NoTimeConversions
import com.twitter.util.TimeConversions._

class WhitelistingServiceSpec extends UnitSpecification with NoTimeConversions {
  sequential

  def afterDuration[A](duration: Duration)(assertion: => MatchResult[A]): MatchResult[A] = {
    Thread.sleep(duration.inMilliseconds)
    assertion
  }

  "The whitelisting service" should {

    trait Context extends AfterEach {
      val config = new BazookaConfig
      val factory = new ZookeeperClientFactory
      val zookeeperClient = factory.create(config)

      override def after = {
        zookeeperClient.delete().deletingChildrenIfNeeded().forPath(s"/${config.getApplicationName}/ratelimits/whitelist")
      }
    }

    trait WhitelistServiceCreation { self: Context =>
      val whitelistingService = new WhitelistingService(zookeeperClient, config.getApplicationName)
    }

    "whitelist a client" in new Context with WhitelistServiceCreation {
      val client = Urn("soundcloud", "applications", "testclient-1")
      Await.result(whitelistingService.whitelistClient(client))
      whitelistingService.hasClientWhitelisted(client) must beTrue
    }

    "unwhitelist a client" in new Context with WhitelistServiceCreation {
      val client = Urn("soundcloud", "applications", "testclient-1")
      Await.result(whitelistingService.whitelistClient(client))
      Await.result(whitelistingService.unwhitelistClient(client))
      whitelistingService.hasClientWhitelisted(client) must beFalse
    }

    "behave idempotently when whitelisting an already whitelisted client" in new Context with WhitelistServiceCreation {
      val client = Urn("soundcloud", "applications", "testclient-1")
      Await.result(whitelistingService.whitelistClient(client))
      Await.result(whitelistingService.whitelistClient(client))
      whitelistingService.hasClientWhitelisted(client) must beTrue
    }

    "behave idempotently when unwhitelisting a client currently not in the whitelist" in new Context with WhitelistServiceCreation {
      val client = Urn("soundcloud", "applications", "testclient-1")
      Await.result(whitelistingService.unwhitelistClient(client))
      whitelistingService.hasClientWhitelisted(client) must beFalse
    }

    "return all the whitelisted clients" in new Context with WhitelistServiceCreation {
      val client1 = Urn("soundcloud", "applications", "testclient-1")
      val client2 = Urn("soundcloud", "applications", "testclient-2")
      val client3 = Urn("soundcloud", "applications", "testclient-3")
      Await.result(whitelistingService.whitelistClient(client3))
      Await.result(whitelistingService.whitelistClient(client2))
      Await.result(whitelistingService.whitelistClient(client1))
      Await.result(whitelistingService.unwhitelistClient(client2))
      whitelistingService.whitelistedClients.toSeq ==== Seq(client1, client3)
    }

    "service must build initial whitelist cache from ZooKeeper state" in new Context {
      val anotherClient = factory.create(config)
      anotherClient.create().forPath(s"/${config.getApplicationName}/ratelimits/whitelist/test-client-1")
      anotherClient.create().forPath(s"/${config.getApplicationName}/ratelimits/whitelist/test-client-2")
      val freshService = new WhitelistingService(zookeeperClient, config.getApplicationName)
      freshService.hasClientWhitelisted(Urn("soundcloud", "applications", "test-client-1")) must beTrue
      freshService.hasClientWhitelisted(Urn("soundcloud", "applications", "test-client-2")) must beTrue

      Executors.newSingleThreadExecutor().submit(new Runnable {
        override def run(): Unit = {
          anotherClient.create().forPath(s"/${config.getApplicationName}/ratelimits/whitelist/test-client-3")
        }
      })
      freshService.hasClientWhitelisted(Urn("soundcloud", "applications", "test-client-3")) must beTrue.eventually(
        retries = 3, sleep = org.specs2.time.Duration.fromScalaDuration(scala.concurrent.duration.Duration("100 milliseconds"))
      )
    }

    "In an event that the base path is tampered with externally, the whitelist will never receive updates again (doh!)" in new Context with WhitelistServiceCreation {
      val anotherClient = factory.create(config)
      val client1 = Urn("soundcloud", "applications", "testclient-1")
      val client2 = Urn("soundcloud", "applications", "testclient-tampered")
      Await.result(whitelistingService.whitelistClient(client1))
      anotherClient.delete().deletingChildrenIfNeeded().forPath(s"/${config.getApplicationName}/ratelimits/whitelist")
      whitelistingService.hasClientWhitelisted(client1) must beFalse
      anotherClient.create().creatingParentsIfNeeded().forPath(s"/${config.getApplicationName}/ratelimits/whitelist/testclient-tampered")
      afterDuration(1.second) {
        whitelistingService.hasClientWhitelisted(client2) must beFalse // sadly
      }
    }
  }
}
