package com.soundcloud.publicApiStrangler.mapper.liebling

import com.soundcloud.bff.Future
import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.publicApiStrangler.mapping.liebling.LikeInfo
import com.soundcloud.publicApiStrangler.support.mapping.{InputValidation, ObjectMapping}
import com.soundcloud.scalakit._
import com.soundcloud.service.client.LieblingClient
import com.soundcloud.service.response.representation.liebling.{LikesCount, UserLikesCount}

class LikeCountMapper(lieblingClient: LieblingClient) extends Mapper[Urn, LikeInfo]
with InputValidation[Urn, LikeInfo] {
  override def mapNonEmptyInputs(session: UserSession, trackUrns: Set[Urn])(implicit context: MappingContext): Future[Map[Urn, LikeInfo]] = {
    val userLikesCount: Future[UserLikesCount] =
      if (!session.isAnonymous)
        lieblingClient.userLikeCounts(session, trackUrns.toList, session.getUser)
      else
        lieblingClient.likeCounts(session, trackUrns.toList).map(UserLikesCount(Set.empty, _))
    userLikesCount.map { case UserLikesCount(likedTracks, likeCounts) =>
      likeCounts.map { case LikesCount(urn, count) =>
        urn -> new ObjectMapping[(Boolean, Long)]((likedTracks.contains(urn), count)) with LikeInfo
      }.toMap
    }
  }
}

