package com.soundcloud.publicApiStrangler.mapper.similarsounds

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.mapper.search.SearchEntityMapper
import com.soundcloud.publicApiStrangler.mapping.similarsounds.SimilarSoundsMapping
import com.soundcloud.publicApiStrangler.support.mapping.{InputValidation, ObjectMapping}
import com.soundcloud.scalakit.Urn
import com.soundcloud.service.client.SimilarSoundsClient
import com.soundcloud.service.response.representation.SimilarSounds
import com.twitter.util.Future

class SimilarSoundsMapper(
                           similarSoundsClient: SimilarSoundsClient,
                           searchEntityMapperxx: SearchEntityMapper)
  extends Mapper[OffsetBasedPage[Urn], SimilarSoundsMapping]
  with InputValidation[OffsetBasedPage[Urn], SimilarSoundsMapping] {

  /**
   * For a given list of inputs creates a map from OffsetBasedPage to SimilarSoundsMapping.
   * If input track does not exist no Map is returned which leads to a 404 response upstream.
   */
  override def mapNonEmptyInputs(session: UserSession, inputs: Set[OffsetBasedPage[Urn]])
                                (implicit context: MappingContext): Future[Map[OffsetBasedPage[Urn], SimilarSoundsMapping]] = {
    Future.collect(inputs.map {
      input =>
        mapSingleInput(session, input).map {
          opt => opt.map(similarSoundsMapping => Some(input -> similarSoundsMapping)).getOrElse(None)
        }
    }.toList).map(_.flatten.toMap)
  }

  /**
   * Makes a call to similar sounds endpoint and transforms its response into an SimilarSoundsMapping.
   * If input track does not exist None is returned.
   */
  def mapSingleInput(session: UserSession,
                     seedTrack: OffsetBasedPage[Urn])
                    (implicit context: MappingContext): Future[Option[SimilarSoundsMapping]] = {
    val (page, pageSize) = offsetBasedToPageBased(seedTrack.offset, seedTrack.limit)
    similarSoundsClient.fetchSimilar(session, seedTrack.param, page, pageSize, "", None).map {
      opt =>
        opt.map(
          similarSounds =>
            new ObjectMapping[SimilarSounds](similarSounds) with SimilarSoundsMapping {
              override def currentPage: OffsetBasedPage[_] = seedTrack
              override def searchEntityMapper: SearchEntityMapper = searchEntityMapperxx
            }
        )
    }
  }

  /**
   * Naively converts offset based pagination to page based pagination.
   * Note that this is not correct and used as a quick fix to be api compliant to former versions.
   *
   */
  def offsetBasedToPageBased(offset: Int, limit: Int): (Int, Int) = {
    limit match {
      case limit if limit > 0 => ((offset / limit) + 1, limit)
      case _ => (0, 0)
    }
  }

}
