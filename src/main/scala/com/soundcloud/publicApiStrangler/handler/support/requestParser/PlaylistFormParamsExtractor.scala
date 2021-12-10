package com.soundcloud.publicApiStrangler.handler.support.requestParser

import cats.implicits._
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.outcome.{CanBeOutcomeOps, NotValid, Outcome}
import com.soundcloud.publicApiStrangler.service.playlists.UpdatePlaylistArtworkRequest
import com.soundcloud.publicApiStrangler.service.playlists.representation.PlaylistCreateOrUpdate
import com.soundcloud.publicApiStrangler.support.MultipartParamsUtils
import com.soundcloud.publicApiStrangler.support.oauth.RailsLikeParamsParser
import com.twitter.finagle.http.{MediaType, Request}
import com.twitter.util.{Future, Try}

import java.net.URLDecoder

class PlaylistFormParamsExtractor(paramsParser: RailsLikeParamsParser = new RailsLikeParamsParser) {

  private val tracksPattern = """playlist\[tracks\]\[\]\[(\S+)\]""".r

  def playlistFromFormRequest(
      request: HandlerRequest
  ): Outcome[PlaylistCreateOrUpdate] = {
    val playlistPattern = """playlist\[(\S+)\]""".r
    val extractedParams =
      paramsParser.parse(request).map(MultipartParamsUtils.extractFieldsFromParams(playlistPattern, _))

    val tracksParams = request.mediaType match {
      case Some(MediaType.WwwForm) =>
        // extract params as they were passed in the request (Map(playlist[tracks][][id] -> List(1111, 2222, 3333))
        val trackIdKeyToTrackIds = multipartTrackIdKeyToTrackIds(request)
        // turn this into Seq(Map(id -> 1111), Map(id -> 2222), Map(id -> 3333))
        extractPlaylistTrackFieldsFromParams(trackIdKeyToTrackIds)

      case Some(MediaType.MultipartForm) => paramsParser.parse(request).flatMap(extractPlaylistTrackFieldsFromParams)
      case _ => throw new RuntimeException(s"Unexpected request type: ${request.mediaType}")
    }

    val playlistParams = extractedParams.getOrElse(Map.empty)
    Try(PlaylistCreateOrUpdate.fromForm(playlistParams, tracksParams)).outcome.leftMap(_ =>
      NotValid("Could not parse request body.")
    )
  }

  def artworkDataFromRequest(request: HandlerRequest): Future[Option[UpdatePlaylistArtworkRequest]] = {
    paramsParser.parseFilesFromRequest(request, "playlist[artwork_data]").map(_.map(UpdatePlaylistArtworkRequest))
  }

  // We need to do our own parsing of request.contentString in order to keep
  // the order of the playlist tracks passed in the request (request.params changes the order)
  private def multipartTrackIdKeyToTrackIds(request: Request): Map[String, Seq[String]] = {
    URLDecoder
      .decode(request.contentString, "UTF-8")
      .split('&')
      .map(_.split('='))
      .map(arr => (arr.headOption.getOrElse(""), if (arr.length > 1) arr(1) else ""))
      .foldLeft(Map[String, Seq[String]]()) {
        case (acc, attribute) =>
          attribute match {
            case (k, vs) => {
              val merged = acc.getOrElse(k, Seq.empty) ++ Seq(vs)
              acc + (k -> merged)
            }
          }
      }
      .filter(tuple =>
        tuple._1 match {
          case tracksPattern(_) => true
          case _ => false
        }
      )
  }

  private def extractPlaylistTrackFieldsFromParams(
      trackIdKeyToTrackIds: Map[String, Seq[String]]
  ): Option[Seq[Map[String, String]]] = {
    val maybeTrackIds = trackIdKeyToTrackIds.foldLeft(Seq[Map[String, String]]()) {
      case (acc, (multipartTrackIdKey, trackIds)) =>
        multipartTrackIdKey match {
          case tracksPattern(idField) => acc ++ trackIds.map(item => Map(idField -> item))
          case _ => acc
        }
    }
    if (maybeTrackIds.nonEmpty)
      Some(maybeTrackIds)
    else
      None
  }
}
