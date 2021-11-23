package com.soundcloud.publicApiStrangler.client.reposts

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient.Count
import com.soundcloud.publicApiStrangler.client.reposts.TestUserDataGenerator._
import com.soundcloud.publicApiStrangler.test.Helpers._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.Await

class RepostsClientSpec extends UnitSpecification {
  trait Context extends Scope {
    implicit val service = mock[JsonClient]
    implicit val session = new UserSessionBuilder().build()
    val telemetry = Telemetry.defaultInstance
    val exceptionCollector = new ExceptionCollector(telemetry)
    val client = new RepostsClient(service)

    val userUrn = Urn("soundcloud", "users", "1039586181")
    val userUrns = Seq(Urn("soundcloud", "users", "1039586181"))
    val notFoundUserUrn = Urn("soundcloud", "users", "0")

    val playlistUrn = Urn("soundcloud", "playlists", "48786981")
    val playlistsUrns = List(playlistUrn, Urn("soundcloud", "playlists", "5"))
    val notFoundPlaylistUrn = Urn("soundcloud", "playlists", "0")

    val trackUrn = Urn("soundcloud", "tracks", "48786981")
    val tracksUrns = List(trackUrn, Urn("soundcloud", "tracks", "101"))
    val notFoundTrackUrn = Urn("soundcloud", "tracks", "0")

    val requestBodyString = s"""{"user_urn":"${userUrn.toString}"}"""
  }

  "#userLikeCounts" >> {
    "successful response" in new Context() {
      expectOkResponse(
        Path() / "users" / "track_reposts" / "count",
        trackRepostCounts,
        Params("urns" -> userUrns.map(_.toString).mkString(","))
      )
      expectOkResponse(
        Path() / "users" / "playlist_reposts" / "count",
        playListRepostCounts,
        Params("urns" -> userUrns.map(_.toString).mkString(","))
      )
      val actual = Await.result(client.getBulkUserRepostCounts(session, userUrns))
      actual must haveSize(3)
      actual.toSet mustEqual Set(
        Count(Urn("soundcloud", "users", "1039586182"), 8), //ensure values are summed up correctly
        Count(Urn("soundcloud", "users", "1039586185"), 4),
        Count(Urn("soundcloud", "users", "1039586181"), 13)
      )
    }

    "On bad response return empty list" in new Context() {
      expectBadRequestResponse(
        Path() / "users" / "track_reposts" / "count",
        Params("urns" -> userUrns.map(_.toString).mkString(","))
      )
      expectBadRequestResponse(
        Path() / "users" / "playlist_reposts" / "count",
        Params("urns" -> userUrns.map(_.toString).mkString(","))
      )
      val actual = Await.result(client.getBulkUserRepostCounts(session, userUrns))
      actual mustEqual (Seq.empty)
    }

    "successful response when response is unbalanced (the number of track reposts are not equal to playlist reposts)" in new Context() {
      expectOkResponse(
        Path() / "users" / "track_reposts" / "count",
        trackRepostCounts,
        Params("urns" -> userUrns.map(_.toString).mkString(","))
      )
      expectOkResponse(
        Path() / "users" / "playlist_reposts" / "count",
        playListRepostCountOnlyOne,
        Params("urns" -> userUrns.map(_.toString).mkString(","))
      )
      val actual = Await.result(client.getBulkUserRepostCounts(session, userUrns)) //userUrns is a dummy user set, what matters is the expected response
      actual must haveSize(3)
      actual.toSet mustEqual Set(
        Count(Urn("soundcloud", "users", "1039586182"), 8),
        Count(Urn("soundcloud", "users", "1039586185"), 2),
        Count(Urn("soundcloud", "users", "1039586181"), 1)
      )
    }

    "successful processing of max batch size" in new Context() {
      expectOkResponse(
        Path() / "users" / "track_reposts" / "count",
        repostCountResponseLarge,
        Params("urns" -> userUrns.map(_.toString).mkString(","))
      )
      expectOkResponse(
        Path() / "users" / "playlist_reposts" / "count",
        repostCountResponseLarge,
        Params("urns" -> userUrns.map(_.toString).mkString(","))
      )
      val actual = Await.result(client.getBulkUserRepostCounts(session, userUrns))
      actual must haveSize(100)
      actual.map(_.urn.identifier).toSet mustEqual ((for (x <- 1 to 100) yield String.valueOf(x)).toSet)
    }

    "successful grouping in case of large user urn list" in new Context() {
      val actual = client.groupByLimit(generateRandomUsersByLimit(10000))
      actual must haveSize(100)
      actual.map(_.size).toSet mustEqual Set(100)
    }

    "successful grouping in case of odd numbers" in new Context() {
      val actual = client.groupByLimit(generateRandomUsersByLimit(999))
      actual must haveSize(10)
      actual.map(_.size).toSet mustEqual Set(100, 99)
    }

    "successful grouping in case only 1 item" in new Context() {
      val actual = client.groupByLimit(generateRandomUsersByLimit(10))
      actual must haveSize(1)
      actual.map(_.size).toSet mustEqual Set(10)
    }

    "successful grouping in case batch limit of 0" in new Context() {
      val actual = client.groupByLimit(generateRandomUsersByLimit(10), 0)
      actual must haveSize(1)
      actual.map(_.size).toSet mustEqual Set(10)
    }

    "successful grouping in case of invalid batch limit of negative value" in new Context() {
      val actual = client.groupByLimit(generateRandomUsersByLimit(10), -1)
      actual must haveSize(10)
      actual.map(_.size).toSet mustEqual Set(1)
    }
  }
}
