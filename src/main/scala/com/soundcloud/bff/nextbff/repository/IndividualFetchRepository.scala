package com.soundcloud.bff.nextbff.repository

import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.util.Future
import play.api.libs.json.JsValue

trait IndividualFetchRepository[I] extends BulkFetchRepository[I] {

  override def bulkFetch(session: UserSession, params: Set[I]): Future[Map[I, JsValue]] =
    fetchKeys(session, params).map(rejectEmptyResults).map(_.toMap)

  private def rejectEmptyResults(tuples: Seq[(I, Option[JsValue])]) =
    tuples.collect {
      case (key, Some(value)) => key -> value
    }

  private def fetchKeys(session: UserSession, params: Set[I]) =
    Future.collect(params.toList.map {
      param =>
        fetch(session, param).map(jsValue => param -> jsValue)
    })

  def fetch(session: UserSession, input: I): Future[Option[JsValue]]
}
