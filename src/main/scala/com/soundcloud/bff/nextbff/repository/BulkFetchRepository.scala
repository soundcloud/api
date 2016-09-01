package com.soundcloud.bff.nextbff.repository

import com.soundcloud.bff.{Future, JsValue}
import com.soundcloud.jvmkit.UserSession

trait BulkFetchRepository[I] {

  def bulkFetch(session: UserSession, params: Set[I]): Future[Map[I, JsValue]]
}
