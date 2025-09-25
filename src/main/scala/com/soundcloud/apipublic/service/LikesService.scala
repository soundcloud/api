package com.soundcloud.apipublic.service

import com.google.protobuf.timestamp.Timestamp
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.likes.{LikeItem, LikesComparisonUtil}
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.service.playlists.PlaylistRequest
import com.soundcloud.apipublic.service.playlists.representation.Playlist
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.{TrackRepresentation, TrackRepresentationsService}
import com.soundcloud.periskop.client.Severity.Info
import com.soundcloud.jvmkit.module.outcome.{
  ApplicationError,
  GoodOps,
  HttpResponseFields,
  HttpServiceError,
  NotAllowed,
  NotFound,
  NotValid,
  Outcome
}
import com.soundcloud.jvmkit.module.rollout.{Rollout, RolloutFeature}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import proto.soundcloud.likes.api.{
  ChronoDirection,
  ChronoParams,
  GetLikesByUserChronoRequest,
  GetLikesByUserChronoResponse,
  Collection => likesCollection,
  LikesClientProtobuf => LikesClient
}
import proto.soundcloud.likes.api.v2.{
  ChronoParams => v2ChronoParams,
  ChronoResponse => v2ChronoResponse,
  Collection => v2likesCollection,
  GetLikesByUserChronoRequest => v2GetLikesByUserChronoRequest,
  LikesService => v2LikesClient
}
import proto.soundcloud.playlists.api.{LikePlaylistRequest, LikesClientProtobuf => PlaylistLikesClientProtobuf}
import proto.soundcloud.tracks.api.{
  GetTrackLikersPagination,
  GetTrackLikersRequest,
  LikeTrackRequest,
  LikesClientProtobuf => TrackLikesClientProtobuf
}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneOffset}
import scala.util.control.NonFatal

case class CreateLikeResponse()

case class DeleteLikeResponse()

case class TrackLikersResponse(urns: Seq[Urn], nextHRef: Option[String])

class LikesService(
    trackRepresentationsService: TrackRepresentationsService,
    playlistsService: PlaylistsService,
    likesClient: LikesClient,
    v2LikesClient: v2LikesClient,
    tracksClient: TrackLikesClientProtobuf,
    playlistsClient: PlaylistLikesClientProtobuf,
    exceptionCollector: ExceptionCollector,
    rollout: Rollout,
    likesComparisonUtil: LikesComparisonUtil
) {
  private def useLikesV2RolloutFlag = RolloutFeature("shadow-likes-v2")

  def createTrackLike(
      session: UserSession,
      urn: Urn
  ): Future[Outcome[CreateLikeResponse]] = {
    val request = LikeTrackRequest(userSession = Some(session.asProtoSession), trackUrn = urn.toString)

    tracksClient
      .likeTrack(request)
      .map(_ => CreateLikeResponse().good)
      .handle {
        case e @ TwinagleException(_, _, _, _) => ApplicationError.fromTwinagleException(e).bad
      }
  }

  def deleteTrackLike(session: UserSession, urn: Urn): Future[Outcome[DeleteLikeResponse]] = {
    val request = LikeTrackRequest(userSession = Some(session.asProtoSession), trackUrn = urn.toString)

    tracksClient
      .unlikeTrack(request)
      .map(_ => DeleteLikeResponse().good)
      .handle {
        case e @ TwinagleException(_, _, _, _) => ApplicationError.fromTwinagleException(e).bad
      }
  }

  def trackLikers(
      session: UserSession,
      urn: Urn,
      pagination: CursorBasedPagination
  ): Future[Outcome[TrackLikersResponse]] = {
    val requestPagination = GetTrackLikersPagination(pagination.cursor, pagination.pageSize)
    val request = GetTrackLikersRequest(Some(session.asProtoSession), urn.toString, Some(requestPagination))

    tracksClient
      .getTrackLikers(request)
      .map(response => {
        val nextHref = response.cursor.map(pagination.nextPage(_).normalizedHref)
        TrackLikersResponse(response.userUrns.map(Urn.parse(_).get), nextHref).good
      })
      .handle {
        case e @ TwinagleException(_, _, _, _) => ApplicationError.fromTwinagleException(e).bad
      }
  }

  def createPlaylistLike(
      session: UserSession,
      urn: Urn
  ): Future[Outcome[CreateLikeResponse]] = {
    val request = LikePlaylistRequest(userSession = Some(session.asProtoSession), urn = urn.toString)

    playlistsClient
      .likePlaylist(request)
      .map(_ => CreateLikeResponse().good)
      .handle {
        case TwinagleException(ErrorCode.NotFound, _, _, _) => NotFound().bad
        case TwinagleException(ErrorCode.PermissionDenied, _, _, _) => NotAllowed().bad
        case TwinagleException(ErrorCode.ResourceExhausted, _, _, _) =>
          HttpServiceError(HttpResponseFields(Status.TooManyRequests.code)).bad
        case TwinagleException(ErrorCode.InvalidArgument, _, _, _) => NotValid("Invalid request").bad
        case TwinagleException(_, msg, _, _) =>
          throw new RuntimeException(s"unexpected response from playlists: $msg")
      }
  }

  def deletePlaylistLike(session: UserSession, urn: Urn): Future[Outcome[DeleteLikeResponse]] = {
    val request = LikePlaylistRequest(userSession = Some(session.asProtoSession), urn = urn.toString)

    playlistsClient
      .unlikePlaylist(request)
      .map(_ => DeleteLikeResponse().good)
      .handle {
        case TwinagleException(ErrorCode.NotFound, _, _, _) => NotFound().bad
        case TwinagleException(_, msg, _, _) =>
          throw new RuntimeException(s"unexpected response from playlists: $msg")
      }
  }

  def userTracksLikes(
      session: UserSession,
      userUrn: Urn,
      access: AccessParams,
      pagination: CursorBasedPagination
  ): Future[Collection[TrackRepresentation]] = {
    def toCollection(likesPage: GetLikesByUserChronoResponse): Future[Collection[TrackRepresentation]] = {
      val trackRequests = likesPage.items.map(like => TrackRequest(Urn.parse(like.targetUrn).get, None)).toList
      for {
        enrichedTracks <- trackRepresentationsService.tracks(session, trackRequests, access)
      } yield {
        val nextHref =
          if (likesPage.items.isEmpty) None
          else Some(pagination.nextPage(likesPage.items.last.cursor).normalizedHref)

        Collection(enrichedTracks, nextHref)
      }
    }

    val request = GetLikesByUserChronoRequest(
      userUrn = userUrn.toString,
      chronoParams = Some(
        ChronoParams(
          direction = ChronoDirection.DESC,
          limit = Some(pagination.pageSize),
          cursor = pagination.cursor
        )
      ),
      collections = Seq(likesCollection.TRACKS)
    )
    rollout.isActive(useLikesV2RolloutFlag).flatMap {
      case true =>
        fetchAndCompareResults(
          request,
          Some(
            v2ChronoParams(
              direction = v2ChronoParams.Direction.DESC,
              limit = Some(pagination.pageSize),
              cursor = pagination.cursor
            )
          )
        ).flatMap(toCollection)

      case false =>
        likesClient
          .getLikesByUserChrono(request)
          .flatMap(toCollection)
    }
  }

  def userPlaylistsLikes(
      session: UserSession,
      userUrn: Urn,
      pagination: CursorBasedPagination
  ): Future[Collection[Playlist]] = {
    val request = GetLikesByUserChronoRequest(
      userUrn = userUrn.toString,
      chronoParams = Some(
        ChronoParams(
          direction = ChronoDirection.DESC,
          limit = Some(pagination.pageSize),
          cursor = pagination.cursor
        )
      ),
      collections = Seq(likesCollection.PLAYLISTS)
    )
    for {
      likesPage <- likesClient.getLikesByUserChrono(request)
      playlists <- playlistsService.fetchPlaylistsMetadataOnly(
        session,
        likesPage.items.map(like => PlaylistRequest(Urn.parse(like.targetUrn).get, None)).toList
      )
    } yield {
      val nextHref =
        if (likesPage.items.isEmpty) None
        else Some(pagination.nextPage(likesPage.items.last.cursor).normalizedHref)

      Collection(playlists, nextHref)
    }
  }

  private def fetchAndCompareResults(
      request: GetLikesByUserChronoRequest,
      v2ChronoParams: Option[v2ChronoParams]
  ): Future[GetLikesByUserChronoResponse] = {
    for {
      (likes, v2Likes) <- Future.join(
        likesClient.getLikesByUserChrono(request).handle {
          case NonFatal(_) =>
            exceptionCollector
              .addMessage("likes_getLikesByUserChrono", "error fetching from likes", Info, collectRequestBody = true)
            GetLikesByUserChronoResponse()
        },
        v2LikesClient
          .getLikesByUserChrono(
            v2GetLikesByUserChronoRequest(request.userUrn, v2ChronoParams, Seq(v2likesCollection.TRACKS))
          )
          .handle {
            case NonFatal(_) =>
              exceptionCollector
                .addMessage(
                  "v2likes_getLikesByUserChrono",
                  "error fetching from likes v2",
                  Info,
                  collectRequestBody = true
                )
              v2ChronoResponse()
          }
      )
      _ = likesComparisonUtil.compareAndReportChrono(
        "getLikesByUserChrono",
        request.userUrn,
        request.chronoParams,
        likeItemsFromLikes(likes),
        likeItemsFromLikesV2(v2Likes)
      )
    } yield likes
  }

  private def likeItemsFromLikes(likesChronoResponse: GetLikesByUserChronoResponse): Seq[LikeItem] = {
    likesChronoResponse.items.map { item =>
      LikeItem(item.`type`, item.userUrn, item.targetUrn, item.timestamp)
    }
  }

  private def likeItemsFromLikesV2(likesV2ChronoResponse: v2ChronoResponse): Seq[LikeItem] = {
    likesV2ChronoResponse.items.map { item =>
      LikeItem(
        item.`type`,
        item.userUrn,
        item.targetUrn,
        getTimestampWithMilliseconds(item.timestamp)
      )
    }
  }

  private def getTimestampWithMilliseconds(timestamp: Option[Timestamp]): String = {
    val formatter: DateTimeFormatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      .withZone(ZoneOffset.UTC)

    formatter.format(
      Instant.ofEpochSecond(timestamp.map(_.seconds).getOrElse(0), timestamp.map(_.nanos).get)
    )
  }
}
