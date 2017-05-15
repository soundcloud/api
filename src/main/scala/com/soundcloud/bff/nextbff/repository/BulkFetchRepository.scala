package com.soundcloud.bff.nextbff.repository

import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.util.Future
import play.api.libs.json.JsValue

trait BulkFetchRepository[I] {

  def bulkFetch(session: UserSession, params: Set[I]): Future[Map[I, JsValue]]
}
