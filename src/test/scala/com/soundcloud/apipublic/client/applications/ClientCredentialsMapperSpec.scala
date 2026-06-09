package com.soundcloud.apipublic.client.applications

import com.soundcloud.apipublic.test.UnitSpecification
import play.api.libs.json.Json

class ClientCredentialsMapperSpec extends UnitSpecification {

  "ClientCredentialsMapper.fromCreateOrListResponse" >> {
    "parses POST /credentials single-object body" in {
      val body =
        """{"self":{"urn":"soundcloud:credentials:321907"},"application":{"urn":"soundcloud:applications:0"},"client_id":"cid","client_secret":"sec","official":false}"""
      val json = Json.parse(body)
      val creds = ClientCredentialsMapper.fromCreateOrListResponse(json)
      creds must have size 1
      creds.head.urn ==== Some("soundcloud:credentials:321907")
      creds.head.client_id ==== Some("cid")
      creds.head.client_secret ==== Some("sec")
    }

    "parses collection.items list" in {
      val body =
        """{"collection":{"items":[{"self":{"urn":"soundcloud:credentials:1"},"client_id":"a","client_secret":"b"}]}}"""
      val json = Json.parse(body)
      val creds = ClientCredentialsMapper.fromCreateOrListResponse(json)
      creds must have size 1
      creds.head.client_id ==== Some("a")
    }
  }
}
