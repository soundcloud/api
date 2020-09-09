package com.soundcloud.publicApiStrangler.client.support

import java.net.URLDecoder

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Param, Params, UrnParam, UrnsParam}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.support.ResponseHandlers.ListResponse
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import play.api.libs.json.JsObject

/**
  * Base class for client definitions for internal services
  */
trait FetchClient {

  /**
    * Decodes query params before making a get call on the client
    *
    * @param session
    * @param path
    * @param params
    * @param headers
    * @return result of get request
    */
  def fetch(
      service: JsonClient,
      session: UserSession,
      path: Path,
      params: Params = Params.empty,
      headers: Headers = Headers.empty
  ): Future[Response] =
    service.getWithSession(session, path, decodedParams(params), headers)

  def fetchByUrns(
      service: JsonClient,
      session: UserSession,
      path: Path,
      urns: Set[Urn],
      batchSize: Int = 50,
      headers: Headers = Headers.empty
  ): Future[List[JsObject]] = {
    inBatches(urns.toList, batchSize) { urnBatch =>
      fetch(service, session, path, urnBatch, headers).map(ListResponse(_))
    }
  }

  protected def inBatches[T](urns: List[Urn], batchSize: Int)(f: (List[Urn] => Future[List[T]])): Future[List[T]] = {
    Future
      .collect {
        urns.grouped(batchSize).toList.map(f)
      }
      .map(_.flatten.toList)
  }

  private def decodedParams(params: Params): Params = params.map {
    case (k, urn @ UrnParam(_)) => (k, urn: Param)
    case (k, urns @ UrnsParam(_)) => (k, urns: Param)
    case (k, param) => (k, param.value.map(URLDecoder.decode(_, "utf-8")).head: Param)
  }
}
