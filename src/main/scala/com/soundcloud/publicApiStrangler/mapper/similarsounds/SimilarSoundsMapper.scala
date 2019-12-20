package com.soundcloud.publicApiStrangler.mapper.similarsounds

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.SystemPlaylistsClient
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.publicApiStrangler.mapper.search.SearchEntityMapper
import com.soundcloud.publicApiStrangler.support.mapping.{InputValidation, ObjectMapping}
import com.twitter.util.Future
import play.api.libs.json.{JsLookupResult, JsObject, JsValue}

class SimilarSoundsMapper(systemPlaylistsClient: SystemPlaylistsClient, searchEntityMapperxx: SearchEntityMapper)
    extends Mapper[OffsetBasedPage[Urn], SimilarSoundsMapping]
    with InputValidation[OffsetBasedPage[Urn], SimilarSoundsMapping] {

  /**
    * For a given list of inputs creates a map from OffsetBasedPage to SimilarSoundsMapping.
    * If input track does not exist no Map is returned which leads to a 404 response upstream.
    */
  override def mapNonEmptyInputs(session: UserSession, inputs: Set[OffsetBasedPage[Urn]])(
      implicit context: MappingContext
  ): Future[Map[OffsetBasedPage[Urn], SimilarSoundsMapping]] = {
    Future
      .collect(inputs.map { input =>
        mapSingleInput(session, input).map { opt =>
          opt.map(similarSoundsMapping => Some(input -> similarSoundsMapping)).getOrElse(None)
        }
      }.toList)
      .map(_.flatten.toMap)
  }

  /**
    * Makes a call to similar sounds endpoint and transforms its response into an SimilarSoundsMapping.
    * If input track does not exist None is returned.
    */
  def mapSingleInput(session: UserSession, seedTrack: OffsetBasedPage[Urn])(
      implicit context: MappingContext
  ): Future[Option[SimilarSoundsMapping]] = {
    systemPlaylistsClient.fetchSimilar(session, seedTrack.param).map { opt =>
      opt.map(similarSounds =>
        new ObjectMapping[SimilarSounds](similarSounds) with SimilarSoundsMapping {
          override def currentPage: OffsetBasedPage[_] = seedTrack

          override def searchEntityMapper: SearchEntityMapper = searchEntityMapperxx
        }
      )
    }
  }
}

object SimilarSoundsMapper {
  def apply(json: JsValue): SimilarSounds = {
    SimilarSounds(mapTracks((json \ "tracks")), mapMeta(json \ "meta"))
  }

  private def mapTracks(json: JsLookupResult): Iterable[Urn] = {
    json.as[List[JsObject]].map { trackUrn =>
      (trackUrn \ "urn").as[Urn]
    }
  }

  private def mapMeta(json: JsLookupResult): SimilarSoundsMeta = {
    SimilarSoundsMeta(
      (json \ "page_size").as[Int],
      (json \ "variant").as[String],
      (json \ "source_version").as[String],
      (json \ "query_urn").as[Urn]
    )
  }
}
