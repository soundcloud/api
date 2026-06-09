package com.soundcloud.apipublic.handler.representation

import com.soundcloud.apipublic.client.applications.ClientApplicationDetail
import play.api.libs.json.{JsNull, Json, Writes}

case class ClientApplicationsPage(collection: Seq[ClientApplicationDetail])

object ClientApplicationsPage {
  implicit val writesClientApplicationsPage: Writes[ClientApplicationsPage] =
    Writes { page =>
      Json.obj(
        "collection" -> Json.toJson(page.collection),
        "next_href" -> JsNull,
        "query_urn" -> JsNull
      )
    }
}
