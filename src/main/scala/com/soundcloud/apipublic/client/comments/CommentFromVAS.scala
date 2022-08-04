package com.soundcloud.apipublic.client.comments

import com.soundcloud.jvmkit.module.twirp.proto.WellKnownOps.ProtoTimestampExt
import com.soundcloud.jvmkit.module.util.Urn
import org.joda.time.DateTime
import proto.soundcloud.comments.api.{Comment => ProtoComment}

import scala.util.Try

case class CommentFromVAS(
    urn: Urn,
    track: Urn,
    user: Urn,
    createdAt: Option[DateTime],
    timestamp: Option[Long],
    body: String,
    secretToken: Option[String] = None
)

object CommentFromVAS {

  def toInt(o: Option[Long]): Option[Int] = o.flatMap(s => Try(s.toInt).toOption)

  private def toLong(o: Option[Int]): Option[Long] = o.flatMap(s => Try(s.toLong).toOption)

  def fromProto(protoComment: ProtoComment): CommentFromVAS = CommentFromVAS(
    urn = Urn.parse(protoComment.urn).get,
    track = Urn.parse(protoComment.trackUrn).get,
    user = Urn.parse(protoComment.userUrn).get,
    createdAt = protoComment.createdAt.map(_.asJodaDateTime),
    timestamp = toLong(protoComment.timestamp),
    body = protoComment.body
  )
}
