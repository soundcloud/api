package com.soundcloud.publicApiStrangler.service

import java.util.TimeZone

import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.test.util.TrackMetadataTrackBuilder
import com.soundcloud.scalakit.test.UnitSpecification
import org.joda.time.{DateTimeZone, LocalDateTime}

import scala.util.Random

class TrackPaginationSpec extends UnitSpecification {
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  DateTimeZone.setDefault(DateTimeZone.UTC)

  def trackUrns(size: Int) = Random.shuffle((0 until size).map(n => Urn(s"soundcloud:tracks:${n}"))).toList
  def tracks(size: Int) = Random.shuffle((0 until size).map(n => TrackMetadataTrackBuilder(urn = Urn(s"soundcloud:tracks:${n}")).build)).toList

  "empty pagination" >> {
    val pagination = new TrackPagination(None, None, false, None, None)

    "#calculateTrackUrnPage" >> {
      "returns the urns" >> {
        pagination.calculateTrackUrnPage(trackUrns(3)) should containAllOf(List(
          Urn("soundcloud:tracks:2"),
          Urn("soundcloud:tracks:0"),
          Urn("soundcloud:tracks:1")))
      }
    }

    "#calculateFinalPage" >> {
      "returns the sorted tracks" >> {
        pagination.calculateFinalPage(tracks(3)).map(_.urn) ==== List(
          Urn("soundcloud:tracks:2"),
          Urn("soundcloud:tracks:1"),
          Urn("soundcloud:tracks:0"))
      }
    }
  }

  "pagination with limit" >> {
    val pagination = new TrackPagination(Some(2), None, false, None, None)

    "#calculateTrackUrnPage" >> {
      "returns the two most recent urns urns" >> {
        pagination.calculateTrackUrnPage(trackUrns(3)) should containAllOf(List(
          Urn("soundcloud:tracks:2"),
          Urn("soundcloud:tracks:1")))
      }
    }

    "#calculateFinalPage" >> {
      "returns the sorted tracks" >> {
        pagination.calculateFinalPage(tracks(2)).map(_.urn) ==== List(
          Urn("soundcloud:tracks:1"),
          Urn("soundcloud:tracks:0"))
      }
    }
  }

  "pagination with offset" >> {
    val pagination = new TrackPagination(Some(2), Some(2), false, None, None)

    "#calculateTrackUrnPage" >> {
      "returns the two most recent urns urns" >> {
        pagination.calculateTrackUrnPage(trackUrns(6)) should containAllOf(List(
          Urn("soundcloud:tracks:3"),
          Urn("soundcloud:tracks:2")))
      }
    }

    "#calculateFinalPage" >> {
      "returns the sorted tracks" >> {
        pagination.calculateFinalPage(tracks(2)).map(_.urn) ==== List(
          Urn("soundcloud:tracks:1"),
          Urn("soundcloud:tracks:0"))
      }
    }
  }

  "pagination with createdAtFrom and To" >> {
    val from = new LocalDateTime(2017, 1, 1, 10, 0, 0)
    val to = new LocalDateTime(2017, 1, 15, 10, 0, 0)

    val tracksWithCreatedAt = List(
      (1, new LocalDateTime(2017, 1, 1, 9, 0, 0)),
      (2, new LocalDateTime(2017, 1, 1, 11, 0, 0)),
      (3, new LocalDateTime(2017, 1, 10, 9, 0, 0)),
      (4, new LocalDateTime(2017, 1, 15, 9, 0, 0)),
      (5, new LocalDateTime(2017, 1, 15, 10, 0, 0)),
      (6, new LocalDateTime(2017, 1, 20, 10, 0, 0))).map { case (id, createdAt) =>
        TrackMetadataTrackBuilder(urn = Urn(s"soundcloud:tracks:${id}"), created_at = createdAt).build }

    val pagination = new TrackPagination(None, None, false, Some(from), Some(to))

    "#calculateTrackUrnPage" >> {
      "returns the urns" >> {
        val urns = tracksWithCreatedAt.map(_.urn)
        pagination.calculateTrackUrnPage(urns) ==== urns.toSet
      }
    }

    "#calculateFinalPage" >> {
      "returns the sorted tracks" >> {
        pagination.calculateFinalPage(tracksWithCreatedAt).map(_.urn) ==== List(
          Urn("soundcloud:tracks:4"),
          Urn("soundcloud:tracks:3"),
          Urn("soundcloud:tracks:2"))
      }
    }
  }


  "parses from a map" >> {
    def build(m: Map[String, String]) = TrackPagination.fromRequest(m)

    "limit" >> {
      build(Map("limit" -> "1")) ==== TrackPagination(Some(1), None, false, None, None)
      build(Map("limit" -> "1.1")) ==== TrackPagination(None, None, false, None, None)
      build(Map("limit" -> "a")) ==== TrackPagination(None, None, false, None, None)
    }

    "offset" >> {
      build(Map("offset" -> "1")) ==== TrackPagination(None, Some(1), false, None, None)
      build(Map("offset" -> "1.1")) ==== TrackPagination(None, None, false, None, None)
      build(Map("offset" -> "a")) ==== TrackPagination(None, None, false, None, None)
    }

    "linked_partitioning is true when present in any way" >> {
      build(Map("linked_partitioning" -> "")) ==== TrackPagination(None, None, true, None, None)
      build(Map("linked_partitioning" -> "false")) ==== TrackPagination(None, None, true, None, None)
      build(Map("linked_partitioning" -> "1")) ==== TrackPagination(None, None, true, None, None)
      build(Map("linked_partitioning" -> "WHATEVER")) ==== TrackPagination(None, None, true, None, None)
    }

    "created_at[from]" >> {
      def withCreatedAtFrom(d: Option[LocalDateTime]) = TrackPagination(None, None, false, d, None)

      build(Map("created_at[from]" -> "")) ==== withCreatedAtFrom(None)
      build(Map("created_at[from]" -> "aaa")) ==== withCreatedAtFrom(None)
      build(Map("created_at[from]" -> "2017-01-16 07:30:16")) ==== withCreatedAtFrom(Some(new LocalDateTime(2017, 1, 16, 7, 30, 16)))
      build(Map("created_at[from]" -> "2017/01/16 07:30:16 +0010")) ==== withCreatedAtFrom(Some(new LocalDateTime(2017, 1, 16, 7, 20, 16)))
      build(Map("created_at[from]" -> "2017-01-16T07:30:16Z")) ==== withCreatedAtFrom(Some(new LocalDateTime(2017, 1, 16, 7, 30, 16)))
      build(Map("created_at[from]" -> "2017-01-16T07:30:16")) ==== withCreatedAtFrom(Some(new LocalDateTime(2017, 1, 16, 7, 30, 16)))
    }

    "created_at[to]" >> {
      def withCreatedAtTo(d: Option[LocalDateTime]) = TrackPagination(None, None, false, None, d)

      build(Map("created_at[to]" -> "")) ==== withCreatedAtTo(None)
      build(Map("created_at[to]" -> "aaa")) ==== withCreatedAtTo(None)
      build(Map("created_at[to]" -> "2017-01-16 07:30:16")) ==== withCreatedAtTo(Some(new LocalDateTime(2017, 1, 16, 7, 30, 16)))
      build(Map("created_at[to]" -> "2017/01/16 07:30:16 +0010")) ==== withCreatedAtTo(Some(new LocalDateTime(2017, 1, 16, 7, 20, 16)))
      build(Map("created_at[to]" -> "2017-01-16T07:30:16Z")) ==== withCreatedAtTo(Some(new LocalDateTime(2017, 1, 16, 7, 30, 16)))
      build(Map("created_at[to]" -> "2017-01-16T07:30:16")) ==== withCreatedAtTo(Some(new LocalDateTime(2017, 1, 16, 7, 30, 16)))
    }
  }
}
