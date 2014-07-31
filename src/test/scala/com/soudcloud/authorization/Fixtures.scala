package com.soudcloud.authorization

import com.soudcloud.bff.test.fixtures.GlobalXmlFiles
import com.soundcloud.bff.test.UnitSpecification
import play.api.libs.json.{JsArray, JsObject}

trait Fixtures {
  this: UnitSpecification =>

  val singleTrackJson = withContentsOf("public_api", "single_track").as[JsObject]
  val tracksArrayJson = withContentsOf("public_api", "tracks_array").as[JsArray]
  val playlistJson = withContentsOf("public_api", "playlist").as[JsObject]
  val userJson = withContentsOf("public_api", "user").as[JsObject]

  val singleTrackXml = GlobalXmlFiles.load("public_api/single_track").get
  val tracksArrayXml = GlobalXmlFiles.load("public_api/tracks_array").get
  val playlistXml = GlobalXmlFiles.load("public_api/playlist").get
  val userXml = GlobalXmlFiles.load("public_api/user").get

}
