package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.google.protobuf.ByteString
import com.soundcloud.hocuspocus.{HocuspocusService, Image, Kind, Raw}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.publicApiStrangler.client.tracks.{TrackMetadataUpdateResult, TrackRequest}
import com.soundcloud.publicApiStrangler.handler.support.error.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.handler.support.requestParser.{
  TrackArtworkUpdateRequest,
  TrackArtworkUpdateResult,
  TrackAssetDataUpdateRequest,
  TrackMetadataUpdateRequest
}
import com.twitter.util.Future

import scala.collection.immutable.HashSet

class TrackUpdateService(
    trackCoordinatorClient: TrackCoordinatorClient,
    hocuspocusService: HocuspocusService,
    trackRepresentationsService: TrackRepresentationsService
) {

  def updateTrack(
      maybeUpdateAlbumArt: Option[TrackArtworkUpdateRequest],
      maybeUpdateTrackAsset: Option[TrackAssetDataUpdateRequest],
      maybeTrackMetadata: Option[TrackMetadataUpdateRequest],
      trackUrn: Urn,
      session: UserSession
  ): Future[Outcome[TrackRepresentation]] = {
    for {
      (assetUpdateResult, metadataUpdateResult) <- Future.join(
        maybeUpdateTrackAsset match {
          case Some(trackAsset) =>
            trackCoordinatorClient.updateTrackAssetData(trackAsset, session, trackUrn)
          case _ => Future.value(Good(()))
        },
        uploadArtworkToS3AndUpdateMetadata(maybeUpdateAlbumArt, maybeTrackMetadata, trackUrn, session)
      )
    } yield {
      (assetUpdateResult, metadataUpdateResult) match {
        case (Good(_), Good(_)) => metadataUpdateResult
        case (Bad(outcome), _) => outcome.bad
        case (_, Bad(outcome)) => outcome.bad
        case _ => throw new UnhandledOutcomeException()
      }
    }
  }

  private def uploadArtworkToS3AndUpdateMetadata(
      maybeUpdateAlbumArt: Option[TrackArtworkUpdateRequest],
      updateTrackMetadata: Option[TrackMetadataUpdateRequest],
      trackUrn: Urn,
      session: UserSession
  ): Future[Outcome[TrackRepresentation]] = {
    for {
      _ <- Future.Unit
      uploadeImageResponse = maybeUpdateAlbumArt match {
        case Some(artworkMetadata) =>
          hocuspocusService
            .storeImage(
              Raw(Kind.ARTWORKS, ByteString.copyFrom(artworkMetadata.imageData))
            )
            .map(image => createTrackArtworkUpdate(image))

        case _ => Future.None
      }

      updatedTrack <- uploadeImageResponse.flatMap(image =>
        updateTrackMetadataAndArtwork(updateTrackMetadata, image, session, trackUrn)
      )
    } yield updatedTrack

  }

  private def updateTrackMetadataAndArtwork(
      updateTrackMetadata: Option[TrackMetadataUpdateRequest],
      imageMetadata: Option[TrackArtworkUpdateResult],
      session: UserSession,
      trackUrn: Urn
  ): Future[Outcome[TrackRepresentation]] = {
    for {
      updateResult <- trackCoordinatorClient.updateTrack(session, trackUrn, updateTrackMetadata, imageMetadata)
      maybeTrack <- trackRepresentationsService.track(session, new TrackRequest(trackUrn, None))
      result = (maybeTrack, updateResult) match {
        case (Some(track), Good(updateResult)) => Good(buildUpdatedTrackWithNewMetadata(track, updateResult))
        case (_, Bad(outcome)) => outcome.bad
        case _ => NotFound().bad
      }
    } yield result
  }

  private def createTrackArtworkUpdate(createdImage: Image): Option[TrackArtworkUpdateResult] = {
    val s3UrlRegex = "s3://([^/ ]+)/([^/ ]+)".r
    createdImage.originUri match {
      case s3UrlRegex(bucket, filename) => Some(TrackArtworkUpdateResult(bucket, filename))
      case _ => None
    }
  }

  private def getGeoBlockings(geoblockings: List[String]): Option[HashSet[String]] = {
    val blockings = geoblockings.foldLeft(HashSet[String]()) {
      case (acc, value) => acc + value
    }

    if (!blockings.isEmpty) Some(blockings) else None
  }

  private def getIsrc(isrc: Option[String]): Option[Isrc] = {
    isrc.map(isrc => {
      Isrc(isrc)
    })
  }

  def buildUpdatedTrackWithNewMetadata(
      trackRep: TrackRepresentation,
      metadataUpdate: TrackMetadataUpdateResult
  ): TrackRepresentation = {
    trackRep.copy(
      isrc = getIsrc(metadataUpdate.isrc),
      geoblockings = metadataUpdate.geo_blockings.map(getGeoBlockings(_)).getOrElse(None),
      track = trackRep.track.copy(
        title = metadataUpdate.title,
        genre = metadataUpdate.genre,
        public = metadataUpdate.public,
        description = metadataUpdate.description,
        api_streamable = metadataUpdate.api_streamable,
        commentable = metadataUpdate.commentable,
        downloadable = metadataUpdate.downloadable,
        embeddable = metadataUpdate.embeddable,
        label_name = metadataUpdate.label_name,
        license = metadataUpdate.license,
        permalink = metadataUpdate.permalink,
        purchase_title = metadataUpdate.purchase_title,
        purchase_url = metadataUpdate.purchase_url,
        release_day = metadataUpdate.release_day,
        release_month = metadataUpdate.release_month,
        reveal_comments = metadataUpdate.reveal_comments,
        reveal_stats = metadataUpdate.reveal_stats,
        user_tags = metadataUpdate.tag_list.map(_.split(",").toList).getOrElse(List.empty)
      )
    )
  }
}
