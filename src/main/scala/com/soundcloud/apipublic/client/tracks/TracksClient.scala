package com.soundcloud.apipublic.client.tracks

import com.soundcloud.apipublic.handler.comments.CreateCommentParams
import com.soundcloud.jvmkit.module.outcome.OutcomeF
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession

trait TracksClient {
  def createComment(session: UserSession, params: CreateCommentParams): OutcomeF[Urn]
  def getComments(
      session: UserSession,
      trackUrn: Urn,
      secretToken: Option[String],
      offset: Int,
      limit: Int
  ): OutcomeF[Seq[Urn]]
}
