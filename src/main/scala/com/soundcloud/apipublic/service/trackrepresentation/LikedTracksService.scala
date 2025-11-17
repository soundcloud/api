package com.soundcloud.apipublic.service.trackrepresentation

import com.soundcloud.jvmkit.module.rollout.{Rollout, RolloutFeature}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler.FutureExtensions
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.periskop.client.Severity.Info
import com.twitter.util.Future
import proto.soundcloud.likes.api.{IsTargetLikedBatchRequest, LikesClientProtobuf}
import proto.soundcloud.likes.api.v2.{
  AreTargetsLikedByUserRequest,
  AreTargetsLikedByUserResponse,
  LikesService => v2LikesClientProtobuf
}

import scala.util.control.NonFatal

class LikedTracksService(
    likesService: LikesClientProtobuf,
    v2LikesService: v2LikesClientProtobuf,
    exceptionCollector: ExceptionCollector,
    rollout: Rollout,
    batchSize: Int = 50
) {
  private def useLikesV2RolloutFlag = RolloutFeature("likes-v2")

  def getLikedTracks(session: UserSession, trackUrns: Seq[Urn]): Future[Map[Urn, Boolean]] = {
    session.user match {
      case Some(user) =>
        rollout.isActive(useLikesV2RolloutFlag).flatMap {
          case true => getV2LikedTracksInBatches(user, trackUrns)
          case false => getLikedTracksInBatches(user, trackUrns)
        }
      case None => Future.value(Map.empty[Urn, Boolean])
    }
  }

  private def inBatches[T](urns: Set[Urn], batchSize: Int)(f: Set[Urn] => Future[Map[Urn, T]]): Future[Map[Urn, T]] = {
    Future
      .collect {
        urns.grouped(batchSize).toList.map(f)
      }
      .map(_.flatten.toMap)
  }

  private def getLikedTracksInBatches(user: Urn, trackUrns: Seq[Urn]): Future[Map[Urn, Boolean]] = {
    inBatches(trackUrns.toSet, batchSize) { tracks =>
      likesService
        .isTargetLikedBatch(
          IsTargetLikedBatchRequest(
            sourceUrn = user.toString,
            targetUrns = tracks.map(_.toString).toSeq
          )
        )
        .map(likesResponse =>
          tracks.map(trackUrn => trackUrn -> likesResponse.likedTargetUrns.contains(trackUrn.toString)).toMap
        )
        .handleAndReport(exceptionCollector, true) {
          case NonFatal(_) => Map.empty[Urn, Boolean]
        }
    }
  }

  private def getV2LikedTracksInBatches(user: Urn, trackUrns: Seq[Urn]): Future[Map[Urn, Boolean]] = {
    inBatches(trackUrns.toSet, batchSize) { tracks =>
      v2LikesService
        .areTargetsLikedByUser(AreTargetsLikedByUserRequest(user.toString, tracks.map(_.toString).toSeq))
        .handle {
          case NonFatal(_) =>
            exceptionCollector
              .addMessage(
                "v2likes_areTargetsLikedByUser",
                "error fetching from likes v2",
                Info,
                collectRequestBody = true
              )
            AreTargetsLikedByUserResponse()
        }
        .map(likesResponse =>
          tracks.map(trackUrn => trackUrn -> likesResponse.likedTargetUrns.contains(trackUrn.toString)).toMap
        )
    }
  }
}
