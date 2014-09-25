package com.soudcloud.authorization

import com.soudcloud.bff.test.fixtures.GlobalXmlFiles
import com.soundcloud.bff.test.UnitSpecification

trait Fixtures {
  this: UnitSpecification =>

  val singleTrack = withContentsOf("public_api", "single_track")
  val tracksArray = withContentsOf("public_api", "tracks_array")
  val playlist = withContentsOf("public_api", "playlist")
  val user = withContentsOf("public_api", "user")
  val stream = withContentsOf("public_api", "stream")
  val streamFiltered = withContentsOf("public_api", "streamFiltered")
  val generic = withContentsOf("public_api", "generic")
  val genericFiltered = withContentsOf("public_api", "genericFiltered")

  val singleTrackXml = GlobalXmlFiles.load("public_api/single_track").get
  val tracksArrayXml = GlobalXmlFiles.load("public_api/tracks_array").get
  val playlistXml = GlobalXmlFiles.load("public_api/playlist").get
  val userXml = GlobalXmlFiles.load("public_api/user").get

}
