package com.soundcloud.apipublic.client.comments

import com.soundcloud.apipublic.client.comments.{Comment => CommentFromVAS}

import com.soundcloud.jvmkit.module.outcome.OutcomeF
import com.soundcloud.jvmkit.module.util.Urn

trait CommentsClient {
  def getComments(urns: Seq[Urn]): OutcomeF[Seq[CommentFromVAS]]
}
