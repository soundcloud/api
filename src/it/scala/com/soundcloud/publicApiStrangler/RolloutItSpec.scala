package com.soundcloud.publicApiStrangler

import java.security.SecureRandom

import com.soundcloud.jvmkit.config.BazookaConfig
import com.soundcloud.publicApiStrangler.features.RolloutBuilder
import com.soundcloud.publicApiStrangler.test.util.NonUniformRandomDataGenerator
import com.soundcloud.publicApiStrangler.test.util.SeqExtensions.RichSeq
import com.soundcloud.publicApiStrangler.zookeeper.CuratorFrameworkFactory
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.{UnitSpecification, VerifiedMocks}
import com.soundcloud.scalakit.utilities.DebugUtilities.Tappable
import com.twitter.io.Charsets
import org.specs2.matcher.MatchResult

import scala.util.Random

class RolloutItSpec extends UnitSpecification {

  trait FeaturesContext extends VerifiedMocks {
    val config = new BazookaConfig

    val factory = new CuratorFrameworkFactory
    val zookeeperClient = factory.create(config)
    val rollout = RolloutBuilder.build(zookeeperClient, config.getApplicationName)

    def newFeatureName(): String = "feature_" + new Random().nextInt(Integer.MAX_VALUE)
  }

  trait NewFeatureContext extends FeaturesContext {
    val featureName = newFeatureName()
  }

  def ranSufficientNumberOfTimes(f: => Unit): Unit = {
    (0 to 400).foreach(_ => f)
  }

  "returns all the features for the given application" in new FeaturesContext {
    val features = Seq(
      newFeatureName(),
      newFeatureName(),
      newFeatureName()
    )

    features.foreach(featureName => rollout.activate(featureName, 0))

    rollout.allFeatures.keys must containAllOf(features)
  }

  "supports consistent id-based rollouts, for numeric ids" in new NewFeatureContext {
    val sampleSize = 500
    val randomIds = NonUniformRandomDataGenerator
      .positiveInts
      .map(id => Urn("soundcloud", "users", id.toString))
      .take(sampleSize)

    rollout.activate(featureName, 43)

    val activations = randomIds.map(id => rollout.isActiveForId(featureName, Some(id)))
    activations.percentageSatisfying(_ == true).tap("Activations percentage") must beCloseTo(43, delta = 7)
  }

  "supports consistent id-based rollouts, for alphanumeric ids" in new NewFeatureContext {
    val sampleSize = 500
    val randomIds = NonUniformRandomDataGenerator
      .lowerCaseAlphabeticalStrings(stringLength = 7)
      .map(id => Urn("soundcloud", "users", id.toString))
      .take(sampleSize)

    rollout.activate(featureName, 43)

    val activations = randomIds.map(id => rollout.isActiveForId(featureName, Some(id)))
    activations.percentageSatisfying(_ == true).tap("Activations percentage") must beCloseTo(43, delta = 7)
  }

  "returns true if feature is enabled for 100%" in new NewFeatureContext {
    rollout.activate(featureName, 100)
    ranSufficientNumberOfTimes {
      rollout.isActive(featureName) mustEqual true
    }
  }

  "distributes the activation in accordance with the configured percentage" in new NewFeatureContext {
    rollout.activate(featureName, 43)
    val activations = (0 to 400).map(_ => rollout.isActive(featureName))
    activations.percentageSatisfying(_ == true) must beCloseTo(43, delta = 7)
  }

  "is able to activate a feature multiple times with different percentages" in new NewFeatureContext {
    rollout.activate(featureName, 100)
    rollout.isActive(featureName) mustEqual true
    rollout.activate(featureName, 0)
    rollout.isActive(featureName) mustEqual false
  }

  "is disabled if feature doesn't exist or has an activation of 0 percent" in new NewFeatureContext {
    ranSufficientNumberOfTimes {
      rollout.isActive(newFeatureName()) mustEqual false
    }
  }

  "picks up changes done directly in zookeeper" in new NewFeatureContext {
    val anotherClient = factory.create(config)
    rollout.activate(featureName, 0)

    rollout.isActive(featureName) mustEqual false
    anotherClient.setData().forPath("/" + config.getApplicationName + "/rollouts/" + featureName, "100".getBytes(Charsets.Utf8))
    rollout.isActive(featureName) must beTrue.eventually(retries = 3, sleep = 1.second)
  }

  "removes a feature successfully" in new NewFeatureContext {
    rollout.activate(featureName, 100)
    rollout.delete(featureName)
    rollout.isActive(featureName) must beFalse.eventually(retries = 3, sleep = 1.second)
  }

  "ignores non existing features when deleting" in new NewFeatureContext {
    rollout.delete(featureName)
    rollout.isActive(featureName) mustEqual false
  }

  "returns a empty array of features when asking for all features of a non existing application" in new FeaturesContext {
    val nonExistingApplicationRollout = RolloutBuilder.build(zookeeperClient, "nonExistingApplication")
    nonExistingApplicationRollout.allFeatures.size mustEqual 0
  }
}
