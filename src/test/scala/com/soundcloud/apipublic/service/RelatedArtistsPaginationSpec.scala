package com.soundcloud.apipublic.service

import com.soundcloud.apipublic.test.UnitSpecification

import java.net.URL

class RelatedArtistsPaginationSpec extends UnitSpecification {

  "offset and limit" >> {
    "defaults offset to 0 and limit to 50" in {
      val pagination = RelatedArtistsPagination(
        None,
        None,
        linkedPartitioning = false,
        new URL("https://api.soundcloud.com/users/99/related")
      )
      pagination.offset ==== 0
      pagination.limit ==== 50
    }

    "clamps negative offset to 0" in {
      val pagination = RelatedArtistsPagination(
        None,
        Some(-5),
        linkedPartitioning = false,
        new URL("https://api.soundcloud.com/users/99/related")
      )
      pagination.offset ==== 0
    }

    "clamps limit below 1 to 1" in {
      val pagination = RelatedArtistsPagination(
        Some(0),
        None,
        linkedPartitioning = false,
        new URL("https://api.soundcloud.com/users/99/related")
      )
      pagination.limit ==== 1
    }

    "caps limit at 200" in {
      val pagination = RelatedArtistsPagination(
        Some(500),
        None,
        linkedPartitioning = false,
        new URL("https://api.soundcloud.com/users/99/related")
      )
      pagination.limit ==== 200
    }
  }

  "nextHref" >> {
    "does not throw when the request URL has no query string" in {
      val pagination = RelatedArtistsPagination(
        Some(1),
        Some(0),
        linkedPartitioning = true,
        new URL("https://api.soundcloud.com/users/99/related")
      )
      pagination.nextHref(totalAvailable = 10) must beSome[String]
    }

    "drops client_id and sets limit and offset via scala-uri" in {
      val pagination = RelatedArtistsPagination(
        Some(2),
        Some(0),
        linkedPartitioning = true,
        new URL("https://api.soundcloud.com/users/99/related?client_id=secret&q=a%20b&linked_partitioning=true")
      )
      val href = pagination.nextHref(totalAvailable = 10).get
      href must not(contain("client_id"))
      href must contain("limit=2")
      href must contain("offset=2")
      href must contain("linked_partitioning=true")
      href must contain("q=")
    }
  }
}
