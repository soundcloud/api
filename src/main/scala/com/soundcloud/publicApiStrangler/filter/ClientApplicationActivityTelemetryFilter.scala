package com.soundcloud.publicApiStrangler.filter

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, HandlerRouter}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.Routing
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class ClientApplicationActivityTelemetryFilter(
    userAuthentication: UserAuthentication,
    telemetry: Telemetry,
    router: HandlerRouter
) extends SimpleFilter[Request, Response] {
  private val counter = telemetry.counter(
    "incoming_http_requests_by_client_total",
    "Number of incoming HTTP requests by application id",
    "method",
    "path",
    "appid"
  )

  override def apply(request: Request, next: Service[Request, Response]): Future[Response] = {
    val path = router.pathMatching(request).rawPattern

    if (path == Routing.tokenExchangePath) {
      next(request)
    } else {
      userAuthentication.withUserSession(HandlerRequest(request)) { userSession =>
        val clientAppId = Option(userSession.getAgent).map(_.identifier).getOrElse("unknown")

        if (ClientApplicationActivityTelemetryFilter.targetApplicationIds.contains(clientAppId)) {
          counter
            .labels(
              request.method.name,
              path,
              clientAppId
            )
            .inc()

        }
        next(request)
      }
    }
  }

}

object ClientApplicationActivityTelemetryFilter {
  val targetApplicationIds = Set(
    "273212",
    "70783",
    "86099",
    "86099",
    "313806",
    "114708",
    "70409",
    "70991",
    "71009",
    "71407",
    "73024",
    "73193",
    "73804",
    "74184",
    "74524",
    "76136",
    "79689",
    "79949",
    "82022",
    "87036",
    "52857",
    "55914",
    "57826",
    "62870",
    "268065",
    "206446",
    "56326",
    "104239",
    "61606",
    "107007",
    "59092",
    "62618",
    "59092",
    "62618",
    "59092",
    "62618",
    "59092",
    "62618",
    "59092",
    "62618",
    "301145",
    "43133",
    "301145",
    "43133",
    "115045",
    "58762",
    "74705",
    "91376",
    "92851",
    "44408",
    "49073",
    "74705",
    "91376",
    "92851",
    "44408",
    "49073",
    "74705",
    "91376",
    "92851",
    "44408",
    "49073",
    "54932",
    "29029",
    "29029",
    "29029",
    "2486",
    "92100",
    "92900",
    "92100",
    "92900",
    "1592",
    "1837",
    "1837",
    "46921",
    "46921",
    "2194",
    "48431",
    "52493",
    "2635",
    "2827",
    "4587",
    "4591",
    "5119",
    "5284",
    "5285",
    "5521",
    "294590",
    "302225",
    "152937",
    "65120",
    "65957",
    "75391",
    "76466",
    "41357",
    "43722",
    "49478",
    "50331",
    "54194",
    "55353",
    "56337",
    "56797",
    "57130",
    "60014",
    "110521",
    "306482",
    "306483",
    "306484",
    "120094",
    "314069",
    "282176",
    "119230",
    "119231",
    "100484",
    "100486",
    "127842",
    "108890",
    "121754",
    "149394",
    "285584",
    "144813",
    "44588",
    "157824",
    "154925",
    "54791",
    "54791",
    "65621",
    "68580",
    "127958",
    "295147",
    "295949",
    "65186",
    "78725",
    "89180",
    "269840",
    "188848",
    "313902",
    "313905",
    "313956",
    "181380",
    "195691",
    "309504",
    "310464"
  )
}
