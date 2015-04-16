package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.config.BazookaConfig
import com.soundcloud.publicApiStrangler.features.{RolloutBuilder, ZookeeperClient}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.{UnitSpecification, VerifiedMocks}
import com.twitter.io.Charsets

import scala.util.Random

class RolloutItSpec extends UnitSpecification {

  trait FeaturesContext extends VerifiedMocks {
    val config = new BazookaConfig
    val rolloutAddress = config.get("ZOOKEEPER_SERVERS")
    val rollout = RolloutBuilder.build(rolloutAddress, config.getApplicationName)

    def newFeatureName(): String = "feature_" + new Random().nextInt(Integer.MAX_VALUE)
  }

  trait NewFeatureContext extends FeaturesContext {
    val featureName = newFeatureName()
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

  "supports consistent user-based rollouts" in new NewFeatureContext {
    rollout.activate(featureName, 98)
    rollout.isActiveForUser(featureName, Urn("soundcloud:users:997")) mustEqual true
    rollout.isActiveForUser(featureName, Urn("soundcloud:users:999")) mustEqual false
  }
  
  "returns true if feature is enabled for 100%" in new NewFeatureContext {
    rollout.activate(featureName, 100)
    rollout.isActive(featureName) mustEqual true
  }

  "is able to activate a feature multiple times with different percentages" in new NewFeatureContext {
    rollout.activate(featureName, 100)
    rollout.isActive(featureName) mustEqual true
    rollout.activate(featureName, 0)
    rollout.isActive(featureName) mustEqual false
  }

  "is disabled if feature doesn't exist" in new NewFeatureContext {
    rollout.isActive(featureName) mustEqual false
  }

  "picks up changes done directly in zookeeper" in new NewFeatureContext {
    val zookeeper = ZookeeperClient.create(rolloutAddress)

    rollout.activate(featureName, 0)
    rollout.isActive(featureName) mustEqual false
    zookeeper.setData().forPath("/" + config.getApplicationName + "/features/" + featureName, "100".getBytes(Charsets.Utf8))

    rollout.isActive(featureName) mustEqual true
  }

  "removes a feature successfully" in new NewFeatureContext {
    rollout.activate(featureName, 100)
    rollout.delete(featureName)
    rollout.isActive(featureName) mustEqual false

    rollout.allFeatures() must not contain(featureName, any[Int])
  }

  "ignores non existing features when deleting" in new NewFeatureContext {
    rollout.delete(featureName)
    rollout.isActive(featureName) mustEqual false
  }

  "returns a empty array of features when asking for all features of a non existing application" in new FeaturesContext {
    val nonExistingApplicationRollout = RolloutBuilder.build(rolloutAddress, "nonExistingApplication")
    nonExistingApplicationRollout.allFeatures().size mustEqual 0
  }
}