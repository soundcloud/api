package com.soundcloud.apipublic.service

import java.net.URL

import com.soundcloud.jvmkit.module.util.Urn
import io.lemonlabs.uri.Url

import scala.util.Try

case class RelatedArtistsPagination(
    maybeLimit: Option[Int],
    maybeOffset: Option[Int],
    linkedPartitioning: Boolean,
    requestUrl: URL
) {
  val offset: Int = Math.max(maybeOffset.getOrElse(0), 0)
  val limit: Int = Math.max(Math.min(maybeLimit.getOrElse(50), 200), 1)

  def sliceOrderedUrns(urns: List[Urn]): List[Urn] = urns.slice(offset, offset + limit)

  def nextHref(totalAvailable: Int): Option[String] = {
    if (!linkedPartitioning) None
    else {
      val nextOffset = offset + limit
      if (totalAvailable <= nextOffset) None
      else {
        val url = Url
          .parse(requestUrl.toString)
          .removeParams("client_id")
          .replaceParams("limit", limit.toString)
          .replaceParams("offset", nextOffset.toString)
        Some(url.toString)
      }
    }
  }
}

object RelatedArtistsPagination {
  def fromRequest(params: Map[String, String], requestUrl: URL): RelatedArtistsPagination =
    RelatedArtistsPagination(
      Try(params.get("limit").map(_.toInt)).toOption.flatten,
      Try(params.get("offset").map(_.toInt)).toOption.flatten,
      params.contains("linked_partitioning"),
      requestUrl
    )
}
