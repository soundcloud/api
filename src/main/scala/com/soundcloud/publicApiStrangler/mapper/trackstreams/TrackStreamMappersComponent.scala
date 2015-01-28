package com.soundcloud.publicApiStrangler.mapper.trackstreams

trait TrackStreamMappersComponent {

  lazy val trackStreamUrlToJsonResponseMapper = new TrackStreamJsonResponseMapper
  lazy val trackStreamUrlToRedirectMapper = new TrackStreamRedirectResponseMapper

}
