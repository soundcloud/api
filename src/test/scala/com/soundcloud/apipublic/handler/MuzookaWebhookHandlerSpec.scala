package com.soundcloud.apipublic.handler

import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.client.TokenDispenserClient
import com.soundcloud.apipublic.client.TokenDispenserClient.AccessToken
import com.soundcloud.apipublic.client.mothership.MoshimoshiClient
import com.soundcloud.apipublic.handler.muzooka.ImageDownloader
import com.soundcloud.apipublic.test.{HandlerSpecificationScope, UnitSpecification}
import com.soundcloud.hocuspocus.HocuspocusService
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.Handler
import com.soundcloud.jvmkit.module.util.Urn
import com.twitter.finagle.http.Method
import com.twitter.io.Buf
import com.twitter.util.Future

class MuzookaWebhookHandlerSpec extends UnitSpecification {
  val validMuzookaRequestBody =
    """
       {
         "name": "Partenaire Particulier",
         "city": null,
         "province": null,
         "country": null,
         "website": null,
         "bio": null,
         "id": "mz4WJaVPlM",
         "image": [
           {
           "width": 1280,
           "height": 1280,
           "ratio": "1x1",
           "name": "medium1",
           "url": "https://media.muzooka.com/images/",
           "faces": null
           }
         ]
       }
   """
  val invalidArtistMuzookaRequestBody =
    """
       {
         "name": "Partenaire Particulier",
         "city": null,
         "province": null,
         "country": null,
         "website": null,
         "bio": null,
         "id": "mz4WAAAAM",
         "image": [
           {
           "width": 1280,
           "height": 1280,
           "ratio": "1x1",
           "name": "medium1",
           "url": "https://media.muzooka.com/images/",
           "faces": null
           }
         ]
       }
   """
  val noImagesMuzookaRequestBody =
    """
       {
         "name": "Partenaire Particulier",
         "city": null,
         "province": null,
         "country": null,
         "website": null,
         "bio": null,
         "id": "mz4WAAAAM",
         "image": null
       }
   """

  trait Context extends HandlerSpecificationScope {
    private val dispenserClient: TokenDispenserClient = mock[TokenDispenserClient]
    private val hocuspocusService: HocuspocusService = mock[HocuspocusService]
    private val moshimoshiClient: MoshimoshiClient = mock[MoshimoshiClient]
    private val imageDownloader: ImageDownloader = mock[ImageDownloader]
    val session = loggedInSession(Urn("soundcloud", "users", "1"))
    lazy val userAuthentication = new FakeUserAuthentication(session)
    val muzookaWebhookHandler = new MuzookaWebhookHandler(
      "F@keAPI_KEY",
      hocuspocusService,
      moshimoshiClient,
      userAuthentication,
      dispenserClient,
      imageDownloader
    )
    dispenserClient.dispenseAccessToken(any(), any()) returns Future.value(AccessToken("ACCESS_TOKEN"))
    hocuspocusService.storeImage(any()) returns Future.value(null)
    moshimoshiClient.updateUserAvatar(any(), any(), any())
    imageDownloader.downloadImageToBuf(any()) returns Buf.Empty
    override def routingDefinitions(): List[(Method, String, Handler)] =
      Routing.forMuzookaWebhookHandler(muzookaWebhookHandler)
  }

  "#updateAvatars" >> {
    "on valid request" >> {
      trait SuccessContext extends Context {}

      "returns 200 when request is signed properly" in new SuccessContext {
        val requestHeaders = Map("X-Signature" -> "sha1=07917730aa637466caa23ad39250c17b99ce7b25")
        val response = post("/muzooka/webhook", Map.empty, requestHeaders, validMuzookaRequestBody)
        response.status.code ==== 200
        response.contentString ==== "Image updated successfully"
      }
    }

    "on request signed but artist or image not found" >> {
      "returns 404 when request is signed properly but with no images" in new Context {
        val requestHeaders = Map("X-Signature" -> "sha1=312ed4680a963f209e019f7a493376407c5aadaf")
        val response = post("/muzooka/webhook", Map.empty, requestHeaders, noImagesMuzookaRequestBody)
        response.status.code ==== 404
      }

      "returns 404 when request is signed properly but no matched artist" in new Context {
        val requestHeaders = Map("X-Signature" -> "sha1=2a63dcd9c6e72728084c0dac39cf7cd05d110edb")
        val response = post("/muzooka/webhook", Map.empty, requestHeaders, invalidArtistMuzookaRequestBody)
        response.status.code ==== 404
      }
    }

    "on request not signed at all" >> {
      "returns 400 when request is signed properly" in new Context {
        val response = post("/muzooka/webhook", Map.empty, Map.empty, validMuzookaRequestBody)
        response.status.code ==== 403
      }
    }
    "on request signed with wrong signature" >> {
      "returns 400 when request is signed properly" in new Context {
        val requestHeaders = Map("X-Signature" -> "sha1=374355157fe3ba4b879c487ad020cd38ewrong")
        val response = post("/muzooka/webhook", Map.empty, requestHeaders, validMuzookaRequestBody)
        response.status.code ==== 403
      }
    }
  }
}
