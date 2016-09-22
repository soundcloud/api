package com.soundcloud.bff.nextbff.repository

import com.soundcloud.jvmkit.{Urn, UserSession}
import com.twitter.util.Future
import play.api.libs.json.JsObject

trait BulkFetchByUrnWithCustomParamRepository[I] extends BulkFetchRepository[I] {

  override def bulkFetch(session: UserSession, params: Set[I]): Future[Map[I, JsObject]] =
    fetch(session, params).map(
      _.map {
        entityJson =>
          val entityKey = extractUrn(entityJson)
          val inputForId = findParamForUrn(entityKey, params)
          inputForId -> entityJson
      }.toMap
    )

  private def extractUrn(obj: JsObject) =
    new Urn((obj \ "self" \ "urn").as[String])

  def fetch(session: UserSession, params: Set[I]): Future[List[JsObject]]

  def extractUrnFromParam(param: I): Urn

  def findParamForUrn(urn: Urn, params: Set[I]) = params.map(p => extractUrnFromParam(p) -> p).find(_._1 == urn).get._2
}
