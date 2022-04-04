package com.soundcloud.apipublic.service.trackrepresentation

import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler.FutureExtensions
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.util.Future
import proto.soundcloud.likes.api.{IsTargetLikedBatchRequest, LikesClientProtobuf}

import scala.util.control.NonFatal

class LikedTracksService(
    likesService: LikesClientProtobuf,
    exceptionCollector: ExceptionCollector,
    batchSize: Int = 50
) {

  def getLikedTracks(session: UserSession, trackUrns: Seq[Urn]): Future[Map[Urn, Boolean]] = {
    session.user match {
      case Some(user) =>
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
}
