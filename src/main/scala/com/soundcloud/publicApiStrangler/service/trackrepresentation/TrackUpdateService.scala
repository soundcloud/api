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
    uploadArtworkToS3AndUpdateTrack(
      maybeUpdateTrackAsset,
      maybeUpdateAlbumArt,
      maybeTrackMetadata,
      trackUrn,
      session
    )
  }

  private def uploadArtworkToS3AndUpdateTrack(
      maybeUpdateTrackAsset: Option[TrackAssetDataUpdateRequest],
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
        updateTrackMetadataAndArtwork(maybeUpdateTrackAsset, updateTrackMetadata, image, session, trackUrn)
      )
    } yield updatedTrack

  }

  private def updateTrackMetadataAndArtwork(
      maybeUpdateTrackAsset: Option[TrackAssetDataUpdateRequest],
      updateTrackMetadata: Option[TrackMetadataUpdateRequest],
      imageMetadata: Option[TrackArtworkUpdateResult],
      session: UserSession,
      trackUrn: Urn
  ): Future[Outcome[TrackRepresentation]] = {
    for {
      updateResult <- trackCoordinatorClient.updateTrack(
        session,
        trackUrn,
        maybeUpdateTrackAsset,
        updateTrackMetadata,
        imageMetadata
      )
      maybeTrack <- trackRepresentationsService.track(session, new TrackRequest(trackUrn, None))
      result = (updateResult, maybeTrack) match {
        case (Good(updateResult), Some(track)) => Good(buildUpdatedTrackWithNewMetadata(track, updateResult))
        case (Bad(outcome), _) => outcome.bad
        case _ => throw new UnhandledOutcomeException
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
      visibleTrack = trackRep.visibleTrack.copy(
        title = metadataUpdate.title,
        genre = metadataUpdate.genre,
        public = metadataUpdate.public,
        description = metadataUpdate.description,
        apiStreamable = metadataUpdate.api_streamable,
        commentable = metadataUpdate.commentable,
        downloadable = metadataUpdate.downloadable.getOrElse(false),
        embeddable = metadataUpdate.embeddable,
        labelName = metadataUpdate.label_name,
        license = metadataUpdate.license,
        permalink = metadataUpdate.permalink,
        purchaseTitle = metadataUpdate.purchase_title,
        purchaseUrl = metadataUpdate.purchase_url,
        releaseDay = metadataUpdate.release_day,
        releaseMonth = metadataUpdate.release_month,
        revealComments = metadataUpdate.reveal_comments,
        revealStats = metadataUpdate.reveal_stats,
        userTags = metadataUpdate.tag_list.map(_.split(",").toList).getOrElse(List.empty)
      )
    )
  }
}
