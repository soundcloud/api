package com.soundcloud.publicApiStrangler.mapper.trackstreams

trait TrackStreamMappersComponent {

  lazy val trackStreamUrlToJonResponseMapper = new TrackStreamJsonResponseMapper
  lazy val trackStreamUrlToRedirectMapper = new TrackStreamRedirectResponseMapper

}
