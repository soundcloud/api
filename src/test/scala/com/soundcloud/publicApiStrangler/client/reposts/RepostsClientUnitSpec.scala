package com.soundcloud.publicApiStrangler.client.reposts

import com.soundcloud.jvmkit.Urn
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.{NotFoundStatus, OkStatus}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.{Await, Future}
import play.api.libs.json._

class RepostsClientUnitSpec extends UnitSpecification {

  trait Context extends Scope {
    lazy val jsonClient = mock[JsonClient]
    lazy val client = new RepostsClient(jsonClient)
    lazy val user = Urn("soundcloud", "users", "1")
  }

}
