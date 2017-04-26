package com.soundcloud.publicApiStrangler.client.quota

import com.soundcloud.jvmkit.module.http.client.Params
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.BigJvmKitConversions._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.scalakit.finagle.http.{InternalServerErrorStatus, OkStatus}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse}
import com.twitter.util.{Await, Future}
import play.api.libs.json.JsNull

class UserQuotaClientSpec extends UnitSpecification {

  trait Context extends Scope {
    val session = anonymousSession
    val jsonClient = mock[JsonClient]
    val userQuotaClient = new UserQuotaClient(jsonClient)
    val path = Path() / "users" / "quotas"
    val userUrn1 = Urn("soundcloud:users:1")
    val userUrn2 = Urn("soundcloud:users:2")
    val userUrn3 = Urn("soundcloud:users:3")
    val userUrns = Set(userUrn1, userUrn2, userUrn3)
  }

  "downloadsPerTrack" >> {

    "returns empty response in case of exceptions" in new Context {
      jsonClient.get(session, path, toBigJvmKitUrnSet(userUrns), Params.empty) returns
        Future.exception(new Exception("something wonky happened finagle returned exception"))

      val result = Await.result(userQuotaClient.downloadsPerTrack(session, userUrns))
      result must beEmpty
    }

    "returns empty response in case of non 200 responses" in new Context {
      jsonClient.get(session, path, toBigJvmKitUrnSet(userUrns), Params.empty) returns
        Future.value(JsonResponse(InternalServerErrorStatus, JsNull))

      val result = Await.result(userQuotaClient.downloadsPerTrack(session, userUrns))
      result must beEmpty
    }

    "parses the service response when response is 200" in new Context {
      val fixture = withContentsOf("quota", "user_quota_multiple")
      jsonClient.get(session, path, toBigJvmKitUrnSet(userUrns), Params.empty) returns Future.value(JsonResponse(OkStatus, fixture))

      val result = Await.result(userQuotaClient.downloadsPerTrack(session, userUrns))
      result must haveSize(2)
      result must haveKey(userUrn1)
      result(userUrn1) ==== Some(100)
      result must haveKey(userUrn2)
      result(userUrn2) ==== Some(200)
    }

    "if user has unlimited, return None" in new Context {
      val fixture = withContentsOf("quota", "user_quota_multiple_missing_quota")
      jsonClient.get(session, path, toBigJvmKitUrnSet(userUrns), Params.empty) returns Future.value(JsonResponse(OkStatus, fixture))

      val result = Await.result(userQuotaClient.downloadsPerTrack(session, userUrns))
      result must haveSize(2)
      result must haveKey(userUrn2)
      result(userUrn2) ==== None
    }
  }
}
