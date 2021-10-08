package com.soundcloud.publicApiStrangler.handler.support.requestParser

import com.soundcloud.publicApiStrangler.test.UnitSpecification

class TrackAssetDataCreateRequestSpec extends UnitSpecification {
  trait Context extends Scope {
    val emptyMap = Map[String, String]()
    val onlyFileNameMap = Map[String, String]("original_filename" -> "file.name")
    val onlyUidMap = Map[String, String]("uid" -> "does_not_matter")
  }

  "Can handle empty map" in new Context {
    val emptyMapResult = TrackAssetDataCreateRequest.fromForm(emptyMap)
    emptyMapResult === None
  }

  "Can handle only file name" in new Context {
    val onlyFileName = TrackAssetDataCreateRequest.fromForm(onlyFileNameMap)
    onlyFileName === None
  }

  "Can handle only uid" in new Context {
    val onlyUid = TrackAssetDataCreateRequest.fromForm(onlyUidMap)
    onlyUid === None
  }
}
