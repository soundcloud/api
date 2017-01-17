package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.Request


case class PublicApiPaginationParams(limit: Option[Int],
                                     offset: Option[Int],
                                     linkedPartitioning: Boolean,
                                     createdAtFrom: Option[String],
                                     createdAtTo: Option[String]) {}

object PublicApiPaginationParams {
  def fromRequest(req: Request) = {
    PublicApiPaginationParams(
      req.params.getInt("limit"),
      req.params.getInt("offset"),
      req.params.get("linked_partitioning").isDefined,
      req.params.get("created_at[from]"),
      req.params.get("created_at[to]"))
  }
}
