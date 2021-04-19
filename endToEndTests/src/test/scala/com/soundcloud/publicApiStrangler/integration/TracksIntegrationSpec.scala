package com.soundcloud.publicApiStrangler.integration

class TracksIntegrationSpec extends ServerSetup {

  trait TrackContext extends IntegrationContext {
    def path(id: String): String = super.path(s"/tracks/$id")
  }

  "requesting a track resource by default" >> {

    "should return a streamable track" in new TrackContext {
      val response = server.get(path(freeTierTrackId))

      response.status === 200

      (response.json \ "stream_url").as[String] must equalTo(
        s"https://api.soundcloud.com/tracks/$freeTierTrackId/stream"
      )
      (response.json \ "duration").as[Int] must equalTo(189613)
      (response.json \ "streamable").as[Boolean] must equalTo(true)
      (response.json \ "access").as[String] must equalTo("playable")
    }

    "should return a snippet of a track" in new TrackContext {
      val response = server.get(path(highTierTrackId), authenticatedUSHeaders)

      response.status === 200

      (response.json \ "stream_url").as[String] must equalTo(
        s"https://api.soundcloud.com/tracks/$highTierTrackId/stream"
      )
      (response.json \ "duration").as[Int] must equalTo(30000)
      (response.json \ "streamable").as[Boolean] must equalTo(true)
      (response.json \ "access").as[String] must equalTo("preview")
    }

    "should return a track with api_streamable=false" in new TrackContext {
      val response = server.get(path(freeTierNonStreamableTrackId))

      response.status === 200

      (response.json \ "stream_url").as[String] must equalTo(
        s"https://api.soundcloud.com/tracks/$freeTierNonStreamableTrackId/stream"
      )
      (response.json \ "duration").as[Int] must equalTo(7889)
      (response.json \ "streamable").as[Boolean] must equalTo(false)
      (response.json \ "access").as[String] must equalTo("blocked")
    }

    "should return a blocked track" in new TrackContext {
      val response = server.get(path(blockedTrackId), authenticatedUSHeaders)

      (response.json \ "stream_url").as[String] must equalTo(
        s"https://api.soundcloud.com/tracks/$blockedTrackId/stream"
      )
      (response.json \ "access").as[String] must equalTo("blocked")
    }

    "should return a paywalled track, not allowlisted app" in new TrackContext {
      val response = server.get(path(paywalledTrackId), authenticatedUSHeaders)

      (response.json \ "stream_url").as[String] must equalTo(
        s"https://api.soundcloud.com/tracks/$paywalledTrackId/stream"
      )
      (response.json \ "access").as[String] must equalTo("blocked")
    }
  }
}
