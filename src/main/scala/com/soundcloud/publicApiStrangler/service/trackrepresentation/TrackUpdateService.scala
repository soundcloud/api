package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.google.protobuf.ByteString
import com.soundcloud.hocuspocus.{HocuspocusService, Image, Kind, Raw}
import com.soundcloud.jvmkit.module.outcome.{Outcome, _}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.mothership.MoshimoshiClient
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.UserRepresentation
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.trackcoordinator.{TrackCoordinatorClient, TrackCoordinatorTrack}
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.handler.support.error.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.handler.support.requestParser._
import com.twitter.io.Buf
import com.twitter.util.Future

import scala.collection.immutable.HashSet

class TrackUpdateService(
    trackCoordinatorClient: TrackCoordinatorClient,
    moshimoshiClient: MoshimoshiClient,
    hocuspocusService: HocuspocusService,
    trackRepresentationsService: TrackRepresentationsService
) {
  def updateTrack(
      maybeUpdateAlbumArt: Option[TrackArtworkUpdateRequest],
      maybeUpdateTrackAsset: Option[TrackAssetDataUpdateRequest],
      trackMetadata: TrackMetadataUpdateRequest,
      trackUrn: Urn,
      session: UserSession
  ): Future[Outcome[TrackRepresentation]] = {
    for {
      uploadeImageResponse <- uploadArtworkToS3(maybeUpdateAlbumArt)
      trackCoordinatorTrack <- trackCoordinatorClient.updateTrack(
        session,
        trackUrn,
        maybeUpdateTrackAsset,
        trackMetadata,
        uploadeImageResponse
      )
      trackRepresentation <- buildTrackRepresentation(trackCoordinatorTrack, session, trackUrn)
    } yield trackRepresentation
  }

  def createTrack(
      trackAsset: TrackAssetDataCreateRequest,
      maybeAlbumArt: Option[TrackArtworkUpdateRequest],
      trackMetadata: TrackMetadataCreateRequest,
      session: UserSession
  ): Future[Outcome[TrackRepresentation]] = {
    for {
      user <- fetchUser(session, session.getUser)
      uploadedImageResponse <- uploadArtworkToS3(maybeAlbumArt)
      trackCoordinatorTrack <- trackCoordinatorClient.createTrack(
        session,
        trackAsset,
        trackMetadata,
        uploadedImageResponse
      )
      createdTrack <- buildCreatedTrack(session, trackCoordinatorTrack, user)
    } yield createdTrack
  }

  private def fetchUser(session: UserSession, urn: Urn): Future[Outcome[UserRepresentation]] = {
    moshimoshiClient.fetchUserObjects(session, Set(urn)).map {
      case head :: _ => head.good
      case _ => NotFound().bad
    }
  }

  private def uploadArtworkToS3(
      maybeUpdateAlbumArt: Option[TrackArtworkUpdateRequest]
  ): Future[Option[TrackArtworkUpdateResult]] = {
    maybeUpdateAlbumArt match {
      case Some(artworkMetadata) =>
        hocuspocusService
          .storeImage(
            Raw(Kind.ARTWORKS, ByteString.copyFrom(Buf.ByteArray.Owned.extract(artworkMetadata.imageData)))
          )
          .map(image => createTrackArtworkUpdate(image))

      case _ => Future.None
    }
  }

  private def buildCreatedTrack(
      userSession: UserSession,
      trackCoordinatorTrack: Outcome[TrackCoordinatorTrack],
      user: Outcome[UserRepresentation]
  ): Future[Outcome[TrackRepresentation]] = {
    (trackCoordinatorTrack, user) match {
      case (Good(trackCoordinatorTrack), Good(user)) =>
        Future.value(
          Good(TrackRepresentationBuilder.fromTrackCoordinatorTrack(trackCoordinatorTrack, user, userSession.agent))
        )
      case (Bad(outcome), _) => Future.value(outcome.bad)
      case (_, Bad(outcome)) => Future.value(outcome.bad)
      case _ => throw new UnhandledOutcomeException
    }
  }

  private def buildTrackRepresentation(
      trackCoordinatorTrack: Outcome[TrackCoordinatorTrack],
      session: UserSession,
      trackUrn: Urn
  ): Future[Outcome[TrackRepresentation]] = {
    for {
      maybeTrack <- trackRepresentationsService.track(session, TrackRequest(trackUrn, None))
      result = (trackCoordinatorTrack, maybeTrack) match {
        case (Good(trackCoordinatorTrack), Some(track)) =>
          Good(buildUpdatedTrackWithNewMetadata(track, trackCoordinatorTrack))
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

    if (blockings.nonEmpty) Some(blockings) else None
  }

  def buildUpdatedTrackWithNewMetadata(
      trackRep: TrackRepresentation,
      metadataUpdate: TrackCoordinatorTrack
  ): TrackRepresentation = {

    trackRep.copy(
      isrc = metadataUpdate.publisher_metadata.flatMap(publisherMetadata => publisherMetadata.isrc.map(Isrc)),
      availableCountries = metadataUpdate.geo_blockings.flatMap(getGeoBlockings),
      title = metadataUpdate.title,
      genre = metadataUpdate.genre,
      public = metadataUpdate.public,
      description = metadataUpdate.description,
      apiStreamable = metadataUpdate.api_streamable,
      commentable = metadataUpdate.commentable,
      downloadable = metadataUpdate.downloadable.getOrElse(false),
      labelName = metadataUpdate.label_name,
      license = metadataUpdate.license,
      purchaseTitle = metadataUpdate.purchase_title,
      purchaseUrl = metadataUpdate.purchase_url,
      release = metadataUpdate.release,
      releaseDay = metadataUpdate.release_day,
      releaseMonth = metadataUpdate.release_month,
      userTags = metadataUpdate.tag_list
    )
  }
}
