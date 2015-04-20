package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.scalakit.Urn
import com.twitter.util.Future

import scala.collection.immutable.TreeSet

class WhitelistingService {
  def whitelistClient(client: Urn): Future[Unit] = ???
  def unwhitelistClient(client: Urn): Future[Unit] = ???
  def whitelistedClients: TreeSet[Urn] = ???
}
