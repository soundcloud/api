package com.soundcloud.publicApiStrangler.client.trackmetadata

sealed abstract class EmbeddingPermission(stringValue: String)
  extends EnumValue[EmbeddingPermission](stringValue)

object EmbeddingPermission extends Enum[EmbeddingPermission] {
  case object All extends EmbeddingPermission("all")
  case object Me extends EmbeddingPermission("me")
  case object None extends EmbeddingPermission("none")

  lazy val all: Seq[EmbeddingPermission] = Seq(All, Me, None)
}
