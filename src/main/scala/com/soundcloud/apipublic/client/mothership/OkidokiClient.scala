package com.soundcloud.apipublic.client.mothership

import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.Status
import com.twitter.util.{Future, Try}
import play.api.libs.json.{JsObject, JsValue, Json}

import scala.collection.immutable.Seq
import scala.util.control.NonFatal

class OkidokiClient(service: JsonClient, exceptionCollector: ExceptionCollector)
    extends MoshimoshiClient(
      service,
      exceptionCollector: ExceptionCollector
    ) {

  def fetch(session: UserSession, urns: Set[Urn]): Future[List[JsObject]] =
    fetchByUrns(service, session, Path() / "fetch", urns)

  def mutings(session: UserSession, userUrn: Urn): OutcomeF[Seq[Urn]] = {
    if (userUrn == null) {
      return Seq.empty[Urn].goodF
    }

    val path = Path("/users") / userUrn / "mutings" / "urns"

    val mutings = service.getWithSession(session, path).map { response =>
      response.status match {
        case Status.Successful(_) => tryParseOutcome(response.contentString).map(_.as[Seq[Urn]])
        case _ => Seq.empty.good
      }
    } handle {
      case NonFatal(_) =>
        Seq.empty.good
    }

    mutings.outcomeF.catchToUnexpectedError
  }

  private def tryParseOutcome(
      maybeJson: String,
      errorMessage: => String = "Failed to parse response"
  ): Outcome[JsValue] = {
    Try { Json.parse(maybeJson).good }
      .handle[Outcome[JsValue]] { case _ => UnexpectedError(new Exception(errorMessage)).bad }
      .get()
  }
}
