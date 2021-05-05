package com.soundcloud.publicApiStrangler.service.trackrepresentation

import java.net.URL
import java.util.TimeZone

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.tracks.VisibleTrackBuilder
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}

import scala.util.Random

class TrackPaginationSpec extends UnitSpecification with TrackRepresentationSpecContext {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  DateTimeZone.setDefault(DateTimeZone.UTC)

  val baseUrl = new URL("https://api.soundcloud.com")

  def trackUrns(size: Int) = Random.shuffle((0 until size).map(n => Urn("soundcloud", "tracks", n.toString))).toList

  def trackRepresentations(size: Int) =
    Random
      .shuffle(
        (0 until size)
          .map(n =>
            createTrackRepresentationFromVisibleTrack(visibleTrack =
              defaultTrack.copy(urn = Urn("soundcloud", "tracks", n.toString))
            )
          )
      )
      .toList

  "defaults" >> {
    "defaults limit to 50 if not provided" >> {
      new TrackPagination(None, None, false, None, None, baseUrl).limit ==== 50
    }

    "caps limit at 200 if not provided" >> {
      new TrackPagination(Some(201), None, false, None, None, baseUrl).limit ==== 200
    }
  }

  "empty pagination" >> {
    val pagination = new TrackPagination(None, None, false, None, None, baseUrl)

    "#calculateTrackUrnPage" >> {
      "returns the urns" >> {
        pagination.calculateTrackUrnPage(trackUrns(3)) should containAllOf(
          List(Urn("soundcloud", "tracks", "2"), Urn("soundcloud", "tracks", "0"), Urn("soundcloud", "tracks", "1"))
        )
      }
    }

    "#calculateFinalPage" >> {
      "returns the sorted tracks" >> {
        pagination.calculateFinalPage(trackRepresentations(3)).map(_.urn) ==== List(
          Urn("soundcloud", "tracks", "2"),
          Urn("soundcloud", "tracks", "1"),
          Urn("soundcloud", "tracks", "0")
        )
      }
    }
  }

  "pagination with limit" >> {
    val pagination = new TrackPagination(Some(2), None, false, None, None, baseUrl)

    "#calculateTrackUrnPage" >> {
      "returns double the limit of the most recent urns" >> {
        // urns go from 4 to 0
        // with limit 2 then doubled = 4 to 1
        pagination.calculateTrackUrnPage(trackUrns(5)) should containAllOf(
          List(
            Urn("soundcloud", "tracks", "4"),
            Urn("soundcloud", "tracks", "3"),
            Urn("soundcloud", "tracks", "2"),
            Urn("soundcloud", "tracks", "1")
          )
        )
      }
    }

    "#calculateFinalPage" >> {
      "returns the sorted tracks" >> {
        pagination.calculateFinalPage(trackRepresentations(2)).map(_.urn) ==== List(
          Urn("soundcloud", "tracks", "1"),
          Urn("soundcloud", "tracks", "0")
        )
      }
    }
  }

  "#nextHref" >> {
    val base = "https://api.soundcloud.com/tracks?"

    "when linked_partitioning=true" >> {
      "when the number of track urns is greater than the limit + offset" >> {
        "with limit" >> {
          val pagination =
            new TrackPagination(Some(2), None, true, None, None, new URL(s"${base}limit=2&another=value&just-key=&bad"))
          pagination.nextHref(100) ==== Some(s"${base}another=value&just-key=&bad=&offset=2&limit=2")
        }

        "with limit and offset" >> {
          val pagination = new TrackPagination(
            Some(2),
            Some(4),
            true,
            None,
            None,
            new URL(s"${base}limit=2&another=value&just-key=&bad")
          )
          pagination.nextHref(100) ==== Some(s"${base}another=value&just-key=&bad=&offset=6&limit=2")
        }
      }

      "when the number of track urns is smaller than the limit + offset" >> {
        val pagination = new TrackPagination(
          Some(20),
          Some(10),
          true,
          None,
          None,
          new URL(s"${base}limit=2&another=value&just-key=&bad")
        )
        pagination.nextHref(29) ==== None
      }
    }

    "when linked_partitioning=false returns none" >> {
      "when the number of track urns is smaller than the limit + offset" >> {
        val pagination =
          new TrackPagination(Some(2), None, false, None, None, new URL(s"${base}limit=2&another=value&just-key=&bad"))
        pagination.nextHref(100) ==== None
      }
    }
  }

  "pagination with offset" >> {
    val pagination = new TrackPagination(Some(2), Some(2), false, None, None, baseUrl)

    "#calculateTrackUrnPage" >> {
      "returns double the limit of the most recent urns" >> {
        // urns go from 7 to 0
        // with 2 offset = 5 to 0
        // with limit 2 then doubled = 5 to 2
        pagination.calculateTrackUrnPage(trackUrns(8)) ==== Set(
          Urn("soundcloud", "tracks", "5"),
          Urn("soundcloud", "tracks", "4"),
          Urn("soundcloud", "tracks", "3"),
          Urn("soundcloud", "tracks", "2")
        )
      }
    }

    "#calculateFinalPage" >> {
      "returns the sorted tracks" >> {
        pagination.calculateFinalPage(trackRepresentations(4)).map(_.urn) ==== List(
          Urn("soundcloud", "tracks", "3"),
          Urn("soundcloud", "tracks", "2")
        )
      }
    }
  }

  "pagination with createdAtFrom and To" >> {
    val from = new DateTime(2017, 1, 1, 10, 0, 0)
    val to = new DateTime(2017, 1, 15, 10, 0, 0)

    val tracksWithCreatedAt = List(
      (1, new LocalDateTime(2017, 1, 1, 9, 0, 0)),
      (2, new LocalDateTime(2017, 1, 1, 11, 0, 0)),
      (3, new LocalDateTime(2017, 1, 10, 9, 0, 0)),
      (4, new LocalDateTime(2017, 1, 15, 9, 0, 0)),
      (5, new LocalDateTime(2017, 1, 15, 10, 0, 0)),
      (6, new LocalDateTime(2017, 1, 20, 10, 0, 0))
    ).map {
      case (id, createdAt) =>
        (new VisibleTrackBuilder).setUrn(Urn("soundcloud", "tracks", id.toString)).setCreatedAt(createdAt).build
    }

    val trackRepresentationsWithCreatedAt = List(
      (1, new LocalDateTime(2017, 1, 1, 9, 0, 0)),
      (2, new LocalDateTime(2017, 1, 1, 11, 0, 0)),
      (3, new LocalDateTime(2017, 1, 10, 9, 0, 0)),
      (4, new LocalDateTime(2017, 1, 15, 9, 0, 0)),
      (5, new LocalDateTime(2017, 1, 15, 10, 0, 0)),
      (6, new LocalDateTime(2017, 1, 20, 10, 0, 0))
    ).map {
      case (id, createdAt) =>
        createTrackRepresentationFromVisibleTrack(visibleTrack =
          defaultTrack.copy(urn = Urn("soundcloud", "tracks", id.toString), createdAt = createdAt)
        )
    }

    val pagination = new TrackPagination(None, None, false, Some(from), Some(to), baseUrl)

    "#calculateTrackUrnPage" >> {
      "returns the urns" >> {
        val urns = tracksWithCreatedAt.map(_.urn)
        pagination.calculateTrackUrnPage(urns) ==== urns.toSet
      }
    }

    "#calculateFinalPage" >> {
      "returns the sorted tracks" >> {
        pagination
          .calculateFinalPage(trackRepresentationsWithCreatedAt)
          .map(_.urn) ==== List(
          Urn("soundcloud", "tracks", "4"),
          Urn("soundcloud", "tracks", "3"),
          Urn("soundcloud", "tracks", "2")
        )
      }
    }
  }

  "parses from a map" >> {
    val url = baseUrl

    def build(m: Map[String, String]) = TrackPagination.fromRequest(m, url)

    "limit" >> {
      build(Map("limit" -> "1")) ==== TrackPagination(Some(1), None, false, None, None, url)
      build(Map("limit" -> "1.1")) ==== TrackPagination(None, None, false, None, None, url)
      build(Map("limit" -> "a")) ==== TrackPagination(None, None, false, None, None, url)
    }

    "offset" >> {
      build(Map("offset" -> "1")) ==== TrackPagination(None, Some(1), false, None, None, url)
      build(Map("offset" -> "1.1")) ==== TrackPagination(None, None, false, None, None, url)
      build(Map("offset" -> "a")) ==== TrackPagination(None, None, false, None, None, url)
    }

    "linked_partitioning is true when present in any way" >> {
      build(Map("linked_partitioning" -> "")) ==== TrackPagination(None, None, true, None, None, url)
      build(Map("linked_partitioning" -> "false")) ==== TrackPagination(None, None, true, None, None, url)
      build(Map("linked_partitioning" -> "1")) ==== TrackPagination(None, None, true, None, None, url)
      build(Map("linked_partitioning" -> "WHATEVER")) ==== TrackPagination(None, None, true, None, None, url)
    }

    "created_at[from]" >> {
      def withCreatedAtFrom(d: Option[DateTime]) = TrackPagination(None, None, false, d, None, url)

      build(Map("created_at[from]" -> "")) ==== withCreatedAtFrom(None)
      build(Map("created_at[from]" -> "aaa")) ==== withCreatedAtFrom(None)
      build(Map("created_at[from]" -> "2017-01-16 07:30:16")) ==== withCreatedAtFrom(
        Some(new DateTime(2017, 1, 16, 7, 30, 16))
      )
      build(Map("created_at[from]" -> "2017-01-16 07:30:16 +0010")) ==== withCreatedAtFrom(
        Some(new DateTime(2017, 1, 16, 7, 20, 16))
      )
      build(Map("created_at[from]" -> "2017/01/16 07:30:16 +0010")) ==== withCreatedAtFrom(
        Some(new DateTime(2017, 1, 16, 7, 20, 16))
      )
      build(Map("created_at[from]" -> "2017-01-16T07:30:16Z")) ==== withCreatedAtFrom(
        Some(new DateTime(2017, 1, 16, 7, 30, 16))
      )
      build(Map("created_at[from]" -> "2017-01-16T07:30:16")) ==== withCreatedAtFrom(
        Some(new DateTime(2017, 1, 16, 7, 30, 16))
      )
    }

    "created_at[to]" >> {
      def withCreatedAtTo(d: Option[DateTime]) = TrackPagination(None, None, false, None, d, url)

      build(Map("created_at[to]" -> "")) ==== withCreatedAtTo(None)
      build(Map("created_at[to]" -> "aaa")) ==== withCreatedAtTo(None)
      build(Map("created_at[to]" -> "2017-01-16 07:30:16")) ==== withCreatedAtTo(
        Some(new DateTime(2017, 1, 16, 7, 30, 16))
      )
      build(Map("created_at[to]" -> "2017-01-16 07:30:16 +0010")) ==== withCreatedAtTo(
        Some(new DateTime(2017, 1, 16, 7, 20, 16))
      )
      build(Map("created_at[to]" -> "2017/01/16 07:30:16 +0010")) ==== withCreatedAtTo(
        Some(new DateTime(2017, 1, 16, 7, 20, 16))
      )
      build(Map("created_at[to]" -> "2017-01-16T07:30:16Z")) ==== withCreatedAtTo(
        Some(new DateTime(2017, 1, 16, 7, 30, 16))
      )
      build(Map("created_at[to]" -> "2017-01-16T07:30:16")) ==== withCreatedAtTo(
        Some(new DateTime(2017, 1, 16, 7, 30, 16))
      )
    }
  }
}
