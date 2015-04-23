package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.Config
import com.soundcloud.scalakit.cache.Cache
import com.twitter.util.Future

class PrometheusLabelsSafeGuard(cache: Cache, config: Config, appName: ResourceName) {
  
  private[rateLimiting]
  object Constants {
    val uniqueClientsCacheKey = s"${appName.getName}.rateLimit.prometheus.unique_clients"
    val maxPrometheusCounterLabelsConfigKey = "RATELIMITING_MAX_PROMETHEUS_COUNTER_LABELS"
    val maxNrOfLabels = config.get(maxPrometheusCounterLabelsConfigKey).toInt
  }

  def getSafeLabel(client: ApiClient): Future[String] = {
    existingUniqueClientsInCache flatMap { labels =>
      if (labels(client.identifier)) Future.value(client.identifier)
      else if (labels.size < Constants.maxNrOfLabels) updateClientsInCache(labels + client.identifier).map(_ => client.identifier)
      else Future.value("other")
    }
  }

  private def existingUniqueClientsInCache: Future[Set[String]] = {
    cache.get(Constants.uniqueClientsCacheKey) map {
      case Some(value) => value.split(",").toSet
      case None => Set.empty
    }
  }

  private def updateClientsInCache(clients: Set[String]): Future[Unit] = {
    cache.set(Constants.uniqueClientsCacheKey, clients.mkString(","))
  }
}
