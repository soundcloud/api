package com.soundcloud.publicApiStrangler

import java.io.InputStream

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class TrackUploadSpec extends Specification {

  "Public API Strangler" should {
    "properly stream large files" in new Scope {
      // Create stream
      val streamLength = 500000 // 50 MB
      val inputStream: InputStream = new InputStream {
        var count = 0
        def read: Int = {
          count += 1
          if (count > streamLength)
            -1
          else
            0
        }
      }

      // Build request
      val request = new HttpPost(s"http://strangler:5000/tracks?client_id=bcdd94de263f5ce5a929f7a53ee19be7")
      val reqEntity = MultipartEntityBuilder.create()
        .addBinaryBody("track[asset_data]", inputStream, ContentType.APPLICATION_OCTET_STREAM, "donkey_song.mp3")
        .build()
      request.setEntity(reqEntity)

      // Make request
      val httpclient = HttpClients.createDefault()
      val response = httpclient.execute(request)

      // Verify (hash obtained using `cat /dev/zero | head -c 500000 | sha1sum`)
      EntityUtils.toString(response.getEntity) ==== "ok 500000 018684b72a1cae5ba76a9d1a50c337ecb89acb51"
    }
  }
}
