package com.soundcloud.publicApiStrangler

import java.io.InputStream

import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.testutilities.SpinningUpAppSupport
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

class TrackUploadSpec extends UnitSpecification with SpinningUpAppSupport {

  "Public API Strangler" should {
    "properly stream large files" in new Scope {
      // Create stream
      val streamLength = 500000000 // 500 MB
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
      val request = new HttpPost(s"http://${dockerHostName}:5000/tracks")
      val reqEntity = MultipartEntityBuilder.create()
        .addBinaryBody("track[asset_data]", inputStream, ContentType.APPLICATION_OCTET_STREAM, "donkey_song.mp3")
        .build()
      request.setEntity(reqEntity)

      // Make request
      val httpclient = HttpClients.createDefault()
      val response = httpclient.execute(request)

      // Verify (hash obtained using `cat /dev/zero | head -c 500000000 | sha1sum`)
      EntityUtils.toString(response.getEntity) ==== "ok 500000000 7acb6fa3fe6504a77f8683bcbf19fe21579494e1"
    }
  }
}
