package com.soundcloud.publicApiStrangler.features

import java.util.concurrent.ConcurrentHashMap

import com.soundcloud.scalakit.Urn
import com.twitter.io.Charsets
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.{NodeCache, NodeCacheListener}
import org.apache.zookeeper.KeeperException.{NoNodeException, NodeExistsException}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.util.Random

object RolloutBuilder {
  def build(client: CuratorFramework, applicationName: String): Rollout = {
    val zookeeperBaseFeaturesPath = "/" + applicationName + "/features"
    try {
      client.create().creatingParentsIfNeeded().forPath(zookeeperBaseFeaturesPath)
    } catch {
      case _: NodeExistsException =>
    }

    new Rollout(client, zookeeperBaseFeaturesPath)
  }
}

class Rollout(zookeeperCuratorClient: CuratorFramework, zookeeperBaseFeaturesPath: String) {
  private val logger = LoggerFactory.getLogger(this.getClass.getName)

  private val activationsMap = new ConcurrentHashMap[String, Int]

  def activate(featureName: String, percentage: Int): Unit = {
    val activation = percentage.toString.getBytes(Charsets.Utf8)
    try {
      zookeeperCuratorClient.create().creatingParentsIfNeeded().forPath(featurePath(featureName), activation)
      addListenerToFeature(featureName)
    } catch {
      case e: NodeExistsException =>
        zookeeperCuratorClient.setData().forPath(featurePath(featureName), activation)
    }
  }

  def delete(featureName: String): Unit = {
    try {
      zookeeperCuratorClient.delete().forPath(featurePath(featureName))
    } catch {
      case _: NoNodeException =>
    }
  }

  def allFeatures(): Map[String, Int] = {
    zookeeperCuratorClient.getChildren.forPath(zookeeperBaseFeaturesPath).asScala.map { feature =>
      feature -> activationForFeature(feature)
    }.toMap
  }

  def isActive(featureName: String): Boolean = Random.nextInt(100) <= activationForFeature(featureName)

  def isActiveForUser(featureName: String, user: Urn): Boolean = {
    if(activationForFeature(featureName) == 100) {
      true
    } else {
      user.getIdentifier.toLong % 100 < activationForFeature(featureName)
    }
  }

  private def addListenerToFeature(featureName: String): Unit = {
    val featureFlag = new NodeCache(zookeeperCuratorClient, featurePath(featureName))
    featureFlag.getListenable.addListener(new NodeCacheListener {
      override def nodeChanged(): Unit = {
        try {
          val dataFromZNode = featureFlag.getCurrentData
          activationsMap.put(featureName, new String(dataFromZNode.getData, Charsets.Utf8).toInt)
        }
        catch {
          case e: Exception => logger.error("Exception while fetching properties from zookeeper ZNode, reason: " + e.getCause)
        }
      }
    })
  }

  private def featurePath(featureName: String): String = zookeeperBaseFeaturesPath + "/" + featureName

  private def activationForFeature(featureName: String): Int = {
    try {
      Option(activationsMap.get(featureName)) match {
        case Some(percentage) => percentage
        case None => {
          addListenerToFeature(featureName)
          val data = zookeeperCuratorClient.getData.forPath(featurePath(featureName))
          Integer.parseInt(new String(data, Charsets.Utf8))
        }
      }
    } catch {
      case e: NoNodeException => 0
    }
  }
}
