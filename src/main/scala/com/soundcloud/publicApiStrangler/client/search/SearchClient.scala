package com.soundcloud.publicApiStrangler.client.search

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json.Json
import com.soundcloud.jvmkit.module.json.play.UrnFormat._

case class Doc(urn: Urn)

object Doc {
  implicit val format = Json.format[Doc]
}

case class Facet(value: String, count: Long, filter: String)

object Facet {
  implicit val format = Json.format[Facet]
}

case class FacetGroup(name: String, facets: Seq[Facet])

object FacetGroup {
  implicit val format = Json.format[FacetGroup]
}

case class SearchResponse(
    query: String,
    query_urn: Urn,
    offset: Int,
    limit: Int,
    total_results: Long,
    query_time_in_millis: Int,
    docs: Seq[Doc],
    facets: Option[Seq[FacetGroup]]
)

object SearchResponse {
  implicit val format = Json.format[SearchResponse]
}

class SearchClient(jsonClient: JsonClient) {

  def searchTracks(session: UserSession, params: Params, headers: Headers = Headers.empty): Future[SearchResponse] =
    search(session, Path() / "search" / "tracks", params, headers)

  def search(
      session: UserSession,
      path: Path,
      params: Params,
      headers: Headers = Headers.empty
  ): Future[SearchResponse] = {
    jsonClient.getWithSession(session, path, params, headers).map { response =>
      response.status match {
        case Status.Ok => Json.parse(response.contentString).as[SearchResponse]
        case _ => throw new RuntimeException(response.toString)
      }
    }
  }
}
