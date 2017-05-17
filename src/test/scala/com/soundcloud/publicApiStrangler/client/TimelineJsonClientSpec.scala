package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.test.Helpers._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.util.Await

class TimelineJsonClientSpec extends UnitSpecification {

  trait Context extends Scope {
    implicit val service = mock[JsonClient]
    implicit val session = anonymousSession
    val client = new TimelineJsonClient(service)

    val urn = Urn("soundcloud:users:10419549")
    val notFoundUrn = Urn("soundcloud:users:0")
  }

  "#itemStream" >> {
    "with cursor" in new Context {
      expectOkResponse(Path() / "item_stream", timelineItemStream, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "after"))
      timelineItemStream ==== Await.result(client.itemStream(session, Some("deadbeef"), 10))
    }

    "with reverse cursor" in new Context {
      expectOkResponse(Path() / "item_stream", timelineItemStream, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "before"))
      timelineItemStream ==== Await.result(client.itemStream(session, Some("deadbeef"), 10, true))
    }

    "without cursor" in new Context {
      expectOkResponse(Path() / "item_stream", timelineItemStream, Map("page_size" -> "50"))
      timelineItemStream ==== Await.result(client.itemStream(session, None))
    }

    "with cursor encoding and direction" in new Context {
      expectOkResponse(Path() / "item_stream", timelineItemStream, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "before", "cursor_encoding" -> "uuid"))
      timelineItemStream ==== Await.result(client.itemStream(session, Some("deadbeef"), 10, true, Some("uuid")))
    }
  }

  "#stream" >> {
    "with cursor" in new Context {
      expectOkResponse(Path() / "stream", timelineStream, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "after"))
      timelineStream ==== Await.result(client.stream(session, Some("deadbeef"), 10))
    }

    "with reverse cursor" in new Context {
      expectOkResponse(Path() / "stream", timelineStream, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "before"))
      timelineStream ==== Await.result(client.stream(session, Some("deadbeef"), 10, true))
    }

    "without cursor" in new Context {
      expectOkResponse(Path() / "stream", timelineStream, Map("page_size" -> "50"))
      timelineStream ==== Await.result(client.stream(session, None))
    }

    "with cursor encoding and direction" in new Context {
      expectOkResponse(Path() / "stream", timelineStream, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "before", "cursor_encoding" -> "uuid"))
      timelineStream ==== Await.result(client.stream(session, Some("deadbeef"), 10, true, Some("uuid")))
    }
  }

  "#activities" >> {
    "with cursor" in new Context {
      expectOkResponse(Path() / "activities", timelineActivities, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "after"))
      timelineActivities ==== Await.result(client.activities(session, Some("deadbeef"), 10))
    }

    "with reverse cursor" in new Context {
      expectOkResponse(Path() / "activities", timelineActivities, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "before"))
      timelineActivities ==== Await.result(client.activities(session, Some("deadbeef"), 10, true))
    }

    "without cursor" in new Context {
      expectOkResponse(Path() / "activities", timelineActivities, Map("page_size" -> "50"))
      timelineActivities ==== Await.result(client.activities(session, None))
    }
  }

  "#profile" >> {
    "with cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString,
        timelineProfile, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "after"))
      timelineProfile ==== Await.result(client.profile(session, urn, Some("deadbeef"), 10))
    }

    "with reverse cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString,
        timelineProfile, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "before"))
      timelineProfile ==== Await.result(client.profile(session, urn, Some("deadbeef"), 10, reverseCursor = true))
    }

    "without cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString,
        timelineProfile, Map("page_size" -> "50"))
      timelineProfile ==== Await.result(client.profile(session, urn, None))
    }
  }

  "#postedAndRepostedTracks" >> {
    "with cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString / "tracks" / "posted_and_reposted",
        timelinePostedAndRepostedTracks, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "after"))
      timelinePostedAndRepostedTracks ==== Await.result(client.postedAndRepostedTracks(session, urn, Some("deadbeef"), 10))
    }

    "with reverse cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString / "tracks" / "posted_and_reposted",
        timelinePostedAndRepostedTracks, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "before"))
      timelinePostedAndRepostedTracks ==== Await.result(client.postedAndRepostedTracks(session, urn, Some("deadbeef"), 10, reverseCursor = true))
    }

    "without cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString / "tracks" / "posted_and_reposted",
        timelinePostedAndRepostedTracks, Map("page_size" -> "50"))
      timelinePostedAndRepostedTracks ==== Await.result(client.postedAndRepostedTracks(session, urn, None))
    }
  }

  "#postedAndRepostedPlaylists" >> {
    "with cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString / "playlists" / "posted_and_reposted",
        timelinePostedAndRepostedPlaylists, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "after"))
      timelinePostedAndRepostedPlaylists ==== Await.result(client.postedAndRepostedPlaylists(session, urn, Some("deadbeef"), 10))
    }

    "with reverse cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString / "playlists" / "posted_and_reposted",
        timelinePostedAndRepostedPlaylists, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "before"))
      timelinePostedAndRepostedPlaylists ==== Await.result(client.postedAndRepostedPlaylists(session, urn, Some("deadbeef"), 10, reverseCursor = true))
    }

    "without cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString / "playlists" / "posted_and_reposted",
        timelinePostedAndRepostedPlaylists, Map("page_size" -> "50"))
      timelinePostedAndRepostedPlaylists ==== Await.result(client.postedAndRepostedPlaylists(session, urn, None))
    }
  }

  "#postedAndLikedPlaylists" >> {
    "with cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString / "playlists" / "posted_and_liked",
        timelinePostedAndLikedPlaylists, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "after"))
      timelinePostedAndLikedPlaylists ==== Await.result(client.postedAndLikedPlaylists(session, urn, Some("deadbeef"), 10))
    }

    "with reverse cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString / "playlists" / "posted_and_liked",
        timelinePostedAndLikedPlaylists, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "before"))
      timelinePostedAndLikedPlaylists ==== Await.result(client.postedAndLikedPlaylists(session, urn, Some("deadbeef"), 10, reverseCursor = true))
    }

    "without cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString / "playlists" / "posted_and_liked",
        timelinePostedAndLikedPlaylists, Map("page_size" -> "50"))
      timelinePostedAndLikedPlaylists ==== Await.result(client.postedAndLikedPlaylists(session, urn, None))
    }
  }

  "#reposts" >> {
    "with cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString / "reposts",
        timelineReposts, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "after"))
      timelineReposts ==== Await.result(client.reposts(session, urn, Some("deadbeef"), 10))
    }

    "with reverse cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString / "reposts",
        timelineReposts, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "before"))
      timelineReposts ==== Await.result(client.reposts(session, urn, Some("deadbeef"), 10, reverseCursor = true))
    }

    "without cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString / "reposts",
        timelineReposts, Map("page_size" -> "50"))
      timelineReposts ==== Await.result(client.reposts(session, urn, None))
    }
  }

  "#likes" >> {
    "with cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString / "likes",
        timelineLikes, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "after"))
      timelineLikes ==== Await.result(client.likes(session, urn, Some("deadbeef"), 10))
    }

    "with reverse cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString / "likes",
        timelineLikes, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "before"))
      timelineLikes ==== Await.result(client.likes(session, urn, Some("deadbeef"), 10, reverseCursor = true))
    }

    "without cursor" in new Context {
      expectOkResponse(Path() / "profiles" / urn.toString / "likes",
        timelineLikes, Map("page_size" -> "50"))
      timelineLikes ==== Await.result(client.likes(session, urn, None))
    }
  }

  "#followingsTracks" >> {
    "with cursor" in new Context {
      expectOkResponse(Path() / "followings_tracks", timelineStream, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "after"))
      timelineStream ==== Await.result(client.followingsTracks(session, Some("deadbeef"), 10))
    }

    "with reverse cursor" in new Context {
      expectOkResponse(Path() / "followings_tracks", timelineStream, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "before"))
      timelineStream ==== Await.result(client.followingsTracks(session, Some("deadbeef"), 10, true))
    }

    "without cursor" in new Context {
      expectOkResponse(Path() / "followings_tracks", timelineStream, Map("page_size" -> "50"))
      timelineStream ==== Await.result(client.followingsTracks(session, None))
    }

    "with cursor encoding and direction" in new Context {
      expectOkResponse(Path() / "followings_tracks", timelineStream, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "before", "cursor_encoding" -> "uuid"))
      timelineStream ==== Await.result(client.followingsTracks(session, Some("deadbeef"), 10, true, Some("uuid")))
    }
  }

}
