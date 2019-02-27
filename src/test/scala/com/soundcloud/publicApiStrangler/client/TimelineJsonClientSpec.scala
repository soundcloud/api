package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.test.Helpers._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.util.Await
import play.api.libs.json.Json

class TimelineJsonClientSpec extends UnitSpecification {

  trait Context extends Scope {
    implicit val service = mock[JsonClient]
    implicit val session = anonymousSession
    val client = new TimelineJsonClient(service)

    val urn = Urn("soundcloud", "users", "10419549")
    val notFoundUrn = Urn("soundcloud", "users", "0")
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

    trait TimelineWithMixedItemsScenario extends Context {
      val actualTimeline = Json.parse(
        """
             {
                 "events":[
                    {
                       "type":"track:repost",
                       "timestamp":"2014/08/12 08:26:26 +0000",
                       "urn":"soundcloud:tracks:131352352",
                       "actor":"soundcloud:users:70167771",
                       "target":"soundcloud:users:6457573"
                    },
                    {
                       "type":"track",
                       "timestamp":"2014/08/12 06:09:34 +0000",
                       "urn":"soundcloud:tracks:162777787",
                       "actor":"soundcloud:users:18228",
                       "target":null
                    },
                    {
                       "type":"playlist:repost",
                       "timestamp":"2014/08/12 02:28:18 +0000",
                       "urn":"soundcloud:playlists:162755085",
                       "actor":"soundcloud:users:23545583",
                       "target":"soundcloud:users:41447243"
                    },
                    {
                       "type":"playlist",
                       "timestamp":"2014/08/12 01:16:44 +0000",
                       "urn":"soundcloud:playlists:162750218",
                       "actor":"soundcloud:users:5539303",
                       "target":null
                    }
                 ],
                 "meta":{
                    "next_page_cursor":"15"
                 }
              }""")

      val expectedAnswer = Json.parse(
        """
             {
                 "events":[
                    {
                       "type":"track",
                       "timestamp":"2014/08/12 06:09:34 +0000",
                       "urn":"soundcloud:tracks:162777787",
                       "actor":"soundcloud:users:18228",
                       "target":null
                    }
                 ],
                 "meta":{}
              }""")
    }

    trait TimelineWithTracksOnlyScenario extends Context {
      val actualTimeline = Json.parse(
        """
             {
                 "events":[
                    {
                       "type":"track",
                       "timestamp":"2014/08/12 06:09:34 +0000",
                       "urn":"soundcloud:tracks:162777787",
                       "actor":"soundcloud:users:18228",
                       "target":null
                    }
                 ],
                 "meta":{
                    "next_page_cursor":"15"
                 }
              }""")

      val expectedAnswer = Json.parse(
        """
             {
                 "events":[
                    {
                       "type":"track",
                       "timestamp":"2014/08/12 06:09:34 +0000",
                       "urn":"soundcloud:tracks:162777787",
                       "actor":"soundcloud:users:18228",
                       "target":null
                    }
                 ],
                 "meta":{}
              }""")
    }

    "with cursor" in new TimelineWithTracksOnlyScenario {
      expectOkResponse(Path() / "stream", actualTimeline, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "after"))

      expectedAnswer ==== Await.result(client.followingsTracks(session, Some("deadbeef"), 10))
    }

    "with reverse cursor" in new TimelineWithTracksOnlyScenario {
      expectOkResponse(Path() / "stream", actualTimeline, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "before"))

      expectedAnswer ==== Await.result(client.followingsTracks(session, Some("deadbeef"), 10, true))
    }

    "without cursor" in new TimelineWithTracksOnlyScenario {
      expectOkResponse(Path() / "stream", actualTimeline, Map("page_size" -> "50"))

      expectedAnswer ==== Await.result(client.followingsTracks(session, None))
    }

    "with cursor encoding and direction" in new TimelineWithTracksOnlyScenario {
      expectOkResponse(Path() / "stream", actualTimeline, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "before", "cursor_encoding" -> "uuid"))

      expectedAnswer ==== Await.result(client.followingsTracks(session, Some("deadbeef"), 10, true, Some("uuid")))
    }

    "should return tracks only" in new TimelineWithMixedItemsScenario {
      expectOkResponse(Path() / "stream", actualTimeline, Map("page_size" -> "10", "cursor" -> "deadbeef", "direction" -> "after"))

      expectedAnswer ==== Await.result(client.followingsTracks(session, Some("deadbeef"), 10))
    }
  }

}
