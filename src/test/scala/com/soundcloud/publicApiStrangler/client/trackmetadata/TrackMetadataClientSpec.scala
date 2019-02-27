package com.soundcloud.publicApiStrangler.client.trackmetadata

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import org.joda.time.LocalDateTime
import org.mockito.Mockito.{verify, when}
import play.api.libs.json.{JsNull, Json}

class TrackMetadataClientSpec extends UnitSpecification {

  trait Context extends Scope {
    val service = mock[JsonClient]
    val trackmetadataClient = new TrackmetadataClient(service)
  }

  "#track" >> {
    trait TrackContext extends Context {
      val urn = Urn("soundcloud", "tracks", "2")
      val path = Path() / "tracks" / urn
    }

    "track is not found" >> {

      trait NotFoundContext extends TrackContext {
        when(service.getWithSession(anonymousSession, path, Params.empty, Headers.empty))
          .thenReturn(Future(jsonResponse(Status.NotFound, JsNull)))
      }

      "returns None" in new NotFoundContext {
        val track = Await.result(trackmetadataClient.track(anonymousSession, urn))
        track must beNone

        verify(service).getWithSession(anonymousSession, path, Params.empty, Headers.empty)
      }
    }

    "track is found" >> {
      trait FoundContext extends TrackContext {
        when(service.getWithSession(anonymousSession, path, Params.empty, Headers.empty))
          .thenReturn(Future(jsonResponse(Status.Ok, trackmetadataClientTracks_2)))
      }

      "returns the track for the given urn" in new FoundContext {
        val actual = Await.result(trackmetadataClient.track(anonymousSession, urn))
        actual must beSome[Track]
        val track = actual.get

        track.commentable ==== true
        track.created_at ==== LocalDateTime.parse("2007-10-18T11:27:04.000")
        track.description ==== Some("alltime classic")
        track.disabled_at ==== Some(LocalDateTime.parse("2011-01-27T09:00:51.000"))
        track.downloadable ==== Some(false)
        track.duration ==== 85800
        track.genre ==== Some("Dance")
        track.last_modified ==== LocalDateTime.parse("2011-01-27T09:00:51.000")
        track.permalink ==== "jsb"
        track.permalink_url ==== Some("https://soundcloud.com/yvg/jsb")
        track.public ==== false
        track.title ==== "jsb"
        track.user_tags ==== List("music", "spoken words")
        track.machine_tags ==== List("ns:machine_tag=value", "ns:machine_tag=value2")
        track.uid ==== Some("SZFrxdDlaSmh")
        track.urn ==== Urn("soundcloud", "tracks", "2")
        track.user_urn ==== Urn("soundcloud", "users", "435")
        track.api_streamable ==== Some(true)
        track.streamable ==== Some(true)
        track.reveal_comments ==== true
        track.reveal_stats ==== true
        track.label_name ==== Some("some-label")
        track.license ==== "wtfpl"
        track.embeddable ==== Some(true)
        track.release_year ==== Some(1989)
        track.release_month ==== Some(12)
        track.release_day ==== Some(22)
        track.embeddableBy ==== EmbeddingPermission.All
        track.releaseDate ==== Some(new LocalDateTime(1989, 12, 22, 0, 0))
        track.artwork.filename ==== Some("artworks-000001073830-j0xbmn-original.jpg")
        track.published_at ==== Some(new LocalDateTime(1989, 12, 22, 0, 0))

        verify(service).getWithSession(anonymousSession, path, Params.empty, Headers.empty)
      }
    }

    "track with rogue attributes" >> {

      trait RogueTrack extends TrackContext {
        when(service.getWithSession(anonymousSession, path, Params.empty, Headers.empty))
          .thenReturn(Future(jsonResponse(Status.Ok, trackmetadataClientTracks_rogue)))
      }

      "sanitize attributes" in new RogueTrack {
        val response = Await.result(trackmetadataClient.track(anonymousSession, urn))

        response must beSome[Track]
        val track = response.get

        track.description ==== Some("alltime classic")
        track.title ==== "<p> Foo Bar!! </p>"
        track.genre ==== Some("Dance <3")
        track.purchase_title ==== Some("So & So")
        track.label_name ==== Some("Someone@somewhere.com")
        track.track_type ==== Some("something sane")
        track.release ==== Some("<li> Release </li>")
        track.key_signature ==== Some("011ACFDVKFJ011ACFDVKFJ")
      }
    }

    "track where nullable boolean fields are null" >> {

      trait NulledBooleansTrack extends TrackContext {
        when(service.getWithSession(anonymousSession, path, Params.empty, Headers.empty))
          .thenReturn(Future(jsonResponse(Status.Ok, trackmetadataClientNullableBooleans)))
        val response = Await.result(trackmetadataClient.track(anonymousSession, urn))
        response must beSome[Track]
        val track = response.get
      }

      "parses streamable correctly" in new NulledBooleansTrack {
        track.streamable ==== None
      }

      "parses api_streamable correctly" in new NulledBooleansTrack {
        track.api_streamable ==== None
      }

      "parses downloadable correctly" in new NulledBooleansTrack {
        track.downloadable ==== None
        verify(service).getWithSession(anonymousSession, path, Params.empty, Headers.empty)
      }
    }
  }

  "#tracks" >> {

    trait TracksContext extends Context {
      val urn1 = Urn("soundcloud", "tracks", "1")
      val urn2 = Urn("soundcloud", "tracks", "2")
      val urn3 = Urn("soundcloud", "tracks", "3")
      val urns = Set(urn1, urn2, urn3)

      val path = Path() / "tracks"
    }

    "no tracks are found" >> {
      trait NoneFoundContext extends TracksContext {
        when(service.getWithSession(anonymousSession, path, urns, Headers.empty))
          .thenReturn(Future(jsonResponse(Status.Ok, trackmetadataClientEmptyTracks)))
      }

      "returns empty list" in new NoneFoundContext {
        val tracks = Await.result(trackmetadataClient.tracks(anonymousSession, urns))
        tracks must beEmpty

        verify(service).getWithSession(anonymousSession, path, urns, Headers.empty)
      }
    }

    "all of the tracks are found" >> {
      trait NoneFoundContext extends TracksContext {
        when(service.getWithSession(anonymousSession, path, urns, Headers.empty))
          .thenReturn(Future(jsonResponse(Status.Ok, trackmetadataClientMultipleTracks)))
      }

      "returns a list containing all of the tracks" in new NoneFoundContext {
        val tracks = Await.result(trackmetadataClient.tracks(anonymousSession, urns))
        tracks must haveSize(3)
        tracks.map(_.urn) must contain(urn1, urn2, urn3)

        verify(service).getWithSession(anonymousSession, path, urns, Headers.empty)
      }
    }

    "some tracks are found" >> {
      trait SomeFoundContext extends TracksContext {
        when(service.getWithSession(anonymousSession, path, urns, Headers.empty))
          .thenReturn(Future(jsonResponse(Status.Ok, trackmetadataClientTracks_1_3)))
      }

      "returns list of found tracks" in new SomeFoundContext {
        val tracks = Await.result(trackmetadataClient.tracks(anonymousSession, Set(urn1, urn2, urn3)))
        tracks must haveSize(2)
        tracks.map(_.urn) must contain(urn1, urn3)

        verify(service).getWithSession(anonymousSession, path, urns, Headers.empty)
      }
    }

    "Track urn count is more than batch limit" >> {
      trait BatchContext extends TracksContext {
        when(service.getWithSession(anonymousSession, path, Set(urn1, urn2), Headers.empty))
          .thenReturn(Future(jsonResponse(Status.Ok, trackmetadataClientTracks_1_3)))

        when(service.getWithSession(anonymousSession, path, Set(urn3), Headers.empty))
          .thenReturn(Future(jsonResponse(Status.Ok, trackmetadataClientTracks_1_3)))
      }

      "returns list of found tracks" in new BatchContext {
        val tracks = Await.result(trackmetadataClient.tracks(anonymousSession, Set(urn1, urn2, urn3), 2))
        tracks must haveSize(4)

        verify(service).getWithSession(anonymousSession, path, Set(urn1, urn2), Headers.empty)
        verify(service).getWithSession(anonymousSession, path, Set(urn3), Headers.empty)
      }
    }
  }

  "#urnsByUser" >> {
    trait UrnsByUserContext extends Context {
      val userUrn = Urn("soundcloud", "users", "1")
      val urn1 = Urn("soundcloud", "tracks", "1")
      val urn2 = Urn("soundcloud", "tracks", "2")

      val path = Path("/users") / userUrn / "tracks" / "urns"

      val jsonBody = Json.parse(
        s"""
           |{
           |  "data": ["$urn1", "$urn2"]
           |}
         """.stripMargin)
    }

    "200 status" in new UrnsByUserContext {
      when(service.getWithSession(anonymousSession, path, Params.empty, Headers.empty))
        .thenReturn(Future(jsonResponse(Status.Ok, jsonBody)))

      Await.result(trackmetadataClient.urnsByUser(anonymousSession, userUrn)) ==== List(urn1, urn2)
    }

    "500 status" in new UrnsByUserContext {
      when(service.getWithSession(anonymousSession, path, Params.empty, Headers.empty))
        .thenReturn(Future(jsonResponse(Status.InternalServerError, JsNull)))

      Await.result(trackmetadataClient.urnsByUser(anonymousSession, userUrn)) ==== List.empty
    }
  }
}
