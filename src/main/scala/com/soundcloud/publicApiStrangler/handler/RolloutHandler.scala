package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import io.prometheus.client.Counter

class RolloutHandler(useAlternative: () => Future[Boolean], default: Handler, alternative: Handler) {
  def handle(request: HandlerRequest): Future[Response] =
    useAlternative()
      .map {
        case true => alternative
        case false => default
      }
      .flatMap(_(request))
}

object RolloutHandler {
  class InstrumentedPredicate(choice: () => Future[Boolean], counter: Counter, staticValues: Seq[String] = Seq.empty)
      extends (() => Future[Boolean]) {
    def apply(): Future[Boolean] =
      choice().foreach(
        (b: Boolean) => counter.labels((staticValues ++ Seq(b.toString)): _*).inc()
      )
  }
}
