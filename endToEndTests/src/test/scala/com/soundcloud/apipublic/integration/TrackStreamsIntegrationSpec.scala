package com.soundcloud.apipublic.integration

import com.soundcloud.apipublic.testutilities.IntegrationTest

class TrackStreamsIntegrationSpec extends IntegrationTest {

  trait TrackStreamContext extends IntegrationContext {
    def path(id: String): String = super.path(s"/tracks/$id/streams")
  }

  "requesting track's streamable URLs" >> {

    "should return URLs for a streamable track" in new TrackStreamContext {
      val response = server.get(path(freeTierTrackId), authenticatedUSHeaders)

      response.status === 200

      (response.json \ "http_mp3_128_url").as[String] must contain("https://cf-media.sndcdn.com/")
      (response.json \ "hls_mp3_128_url").as[String] mustNotEqual empty
      (response.json \ "hls_opus_64_url").as[String] mustNotEqual empty
      (response.json \ "preview_mp3_128_url").as[String] must contain("https://cf-preview-media.sndcdn.com/preview")
    }

    "should return URLs for a high-tier track" in new TrackStreamContext {
      val response = server.get(path(highTierTrackId), authenticatedUSHeaders)

      response.status === 200

      (response.json \ "http_mp3_128_url").as[String] must contain("https://cf-preview-media.sndcdn.com/preview/")
      (response.json \ "hls_mp3_128_url").as[String] mustNotEqual empty
    }

    "should return an error for a track with api_streamable=false" in new TrackStreamContext {
      val response = server.get(path(freeTierNonStreamableTrackId), authenticatedUSHeaders)

      response.status === 403
    }

    "should return error for a blocked track" in new TrackStreamContext {
      val response = server.get(path(blockedTrackId), authenticatedUSHeaders)

      response.status === 403
      (response.json \ "message").as[String] === "This content is only available on SoundCloud."
    }

    "should return error for a rights-holder restricted track" in new TrackStreamContext {
      val response = server.get(path(rightsholderRestrictedTrackId), authenticatedUSHeaders)

      response.status === 404
    }
  }

}
