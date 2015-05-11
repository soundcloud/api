package com.soundcloud.publicApiStrangler.rateLimiting

import java.util.concurrent.ConcurrentHashMap
import java.util.{Collections, Set => JavaSet}

import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.scalakit.Urn
import com.twitter.util.Future
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.KeeperException.{NoNodeException, NodeExistsException}

import scala.collection.JavaConverters._
import scala.collection.immutable.TreeSet

class WhitelistingService(zookeeperClient: CuratorFramework, applicationName: String) {

  private val logger = SoundCloudLoggerFactory.getLogger(getClass)

  val basePath = s"/$applicationName/ratelimits/whitelist"

  private val whitelistCache: JavaSet[Urn] = Collections.newSetFromMap(new ConcurrentHashMap[Urn, java.lang.Boolean])

  try {
    zookeeperClient.create().creatingParentsIfNeeded().forPath(basePath)
  } catch {
    case _: NodeExistsException =>
  }

  private val childrenCache = new PathChildrenCache(zookeeperClient, basePath, false)

  childrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE)

  private val FullPath = ".*/(.*)".r

  rebuildLocalCache()

  childrenCache.getListenable.addListener(new PathChildrenCacheListener {
    override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = (event.getType, event.getData.getPath) match {
      case (PathChildrenCacheEvent.Type.CHILD_ADDED, FullPath(clientId)) =>
        whitelistCache.add(Urn("soundcloud", "applications", clientId))
        logger.info("Added client to whitelist: {}", clientId)
      case (PathChildrenCacheEvent.Type.CHILD_REMOVED, FullPath(clientId)) =>
        whitelistCache.remove(Urn("soundcloud", "applications", clientId))
        logger.info("Removed client from whitelist: {}", clientId)
      case (PathChildrenCacheEvent.Type.INITIALIZED, FullPath(path)) =>
        rebuildLocalCache()
        logger.info("Rebuilt local cache of whitelisted clients")
      case (eventType, _) =>
        logger.info("Something else happened: {}", eventType)
    }
  })

  private def rebuildLocalCache(): Unit = {
    for {
      data <- childrenCache.getCurrentData.asScala
      FullPath(clientId) = data.getPath
    } whitelistCache.add(Urn("soundcloud", "applications", clientId))
  }


  def whitelistClient(client: Urn): Future[Unit] = Future {
    val path = s"$basePath/${client.getIdentifier}"
    try {
      zookeeperClient.create().withMode(CreateMode.PERSISTENT).forPath(path)
      whitelistCache.add(client)
    } catch {
      case e: NodeExistsException => // idempotent, client already whitelisted
    }
  }

  def unwhitelistClient(client: Urn): Future[Unit] = Future {
    val path = s"$basePath/${client.getIdentifier}"
    try {
      zookeeperClient.delete().forPath(path)
      whitelistCache.remove(client)
    } catch {
      case e: NoNodeException => // idempotent, client already unwhitelisted
    }
  }

  def whitelistedClients: TreeSet[Urn] = {
    whitelistCache.asScala.to[TreeSet]
  }

  def hasClientWhitelisted(client: Urn): Boolean = {
    whitelistCache.contains(client)
  }
}
