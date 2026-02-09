package com.soundcloud.apipublic.integration

import com.soundcloud.apipublic.testutilities.IntegrationTest

class TracksIntegrationSpec extends IntegrationTest {

  trait TrackContext extends IntegrationContext {
    def path(id: String): String = super.path(s"/tracks/$id")
  }

  "requesting a track resource by default" >> {

    "should return a streamable track" in new TrackContext {
      val response = server.get(path(freeTierTrackId), authenticatedDEHeaders)

      response.status === 200

      (response.json \ "stream_url").as[String] must equalTo(
        s"https://api.soundcloud.com/tracks/soundcloud:tracks:$freeTierTrackId/preview"
      )
      (response.json \ "duration").as[Int] must equalTo(189613)
      (response.json \ "streamable").as[Boolean] must equalTo(true)
      (response.json \ "access").as[String] must equalTo("playable")
    }

    "should return a snippet of a track" in new TrackContext {
      val response = server.get(path(highTierTrackId), authenticatedUSHeaders)

      response.status === 200

      (response.json \ "stream_url").as[String] must equalTo(
        s"https://api.soundcloud.com/tracks/soundcloud:tracks:$highTierTrackId/preview"
      )
      (response.json \ "duration").as[Int] must equalTo(30000)
      (response.json \ "streamable").as[Boolean] must equalTo(true)
      (response.json \ "access").as[String] must equalTo("preview")
    }

    "should return a geoblocked track" in new TrackContext {
      val response = server.get(path(geoblockedInGermanyTrackId), authenticatedDEHeaders)

      response.status === 200

      (response.json \ "stream_url").asOpt[String] must beNone
      (response.json \ "access").as[String] must equalTo("blocked")
      (response.json \ "available_country_codes").as[List[String]] must not contain "DE"
    }

    "should return a track with api_streamable=false" in new TrackContext {
      val response = server.get(path(freeTierNonStreamableTrackId), authenticatedDEHeaders)

      response.status === 200

      (response.json \ "stream_url").asOpt[String] must beNone
      (response.json \ "duration").as[Int] must equalTo(7889)
      (response.json \ "streamable").as[Boolean] must equalTo(false)
      (response.json \ "access").as[String] must equalTo("blocked")
    }

    "should return a blocked track, not allowlisted app" in new TrackContext {
      val response = server.get(path(blockedTrackId), authenticatedUSHeaders)

      response.status === 200

      (response.json \ "stream_url").asOpt[String] must beNone
      (response.json \ "access").as[String] must equalTo("blocked")
    }

    "should return an error for a rights-holder restricted track" in new TrackContext {
      val response = server.get(path(rightsholderRestrictedTrackId), authenticatedUSHeaders)

      response.status === 404
    }
  }
}
