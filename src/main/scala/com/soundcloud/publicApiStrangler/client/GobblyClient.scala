package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.Urn.format
import com.soundcloud.scalakit.finagle.http.{ForbiddenStatus, OkStatus, UnauthorizedStatus}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.scalakit.{Path, Urn}
import com.twitter.util.{Future, NonFatal}
import play.api.libs.json.JsValue

// Lifted from Track Coordinator
// TODO: delete all of this :(

trait Result[+A] {
  def map[B](fn: A => B): Result[B]
}

case class Success[A](body: A) extends Result[A] {
  override def map[B](fn: A => B): Result[B] = Success(fn(body))
}

case class Created[A](body: A) extends Result[A] {
  override def map[B](fn: A => B): Result[B] = Created(fn(body))
}

case class Accepted[A](body: A) extends Result[A] {
  override def map[B](fn: A => B): Result[B] = Accepted(fn(body))
}

case object NotFound extends Result[Nothing] {
  override def map[B](fn: Nothing => B): Result[B] = NotFound
}

case class ServerError(errors: Set[Error]) extends Result[Nothing] {
  override def map[B](fn: Nothing => B): Result[B] = ServerError(errors)
}

object ServerError {
  def apply(error: Error*) = new ServerError(Set(error :_*))
}

case object Unauthorized extends Result[Nothing] {
  override def map[B](fn: Nothing => B): Result[B] = Unauthorized
}

case class ClientError(errors: Set[Error]) extends Result[Nothing] {
  override def map[B](fn: Nothing => B): Result[B] = ClientError(errors)
}

object ClientError {
  def apply(error: Error*) = new ClientError(Set(error :_*))
}

case class Error(message: String) extends AnyVal

/**
 * Client for Gobbly
 * https://github.com/soundcloud/gobbly
 */
class GobblyClient(jsonClient: JsonClient) {
  def allTracksManagedByFeedsForWrite(session: UserSession, urns: List[Urn]): Future[Result[Boolean]] =
    tracksFromFeeds(session, urns).map(_.map(trackUrns => trackUrns.toSet == urns.toSet))

  private def tracksFromFeeds(session: UserSession, trackUrns: List[Urn]): Future[Result[List[Urn]]] = {
    val errorResponse = ServerError(Error("Error loading managed by feeds status from gobbly"))

    jsonClient.get(session, Path() / "soundcloud-tracks", Params("urns" -> trackUrns), Params.empty).map {
      case JsonResponse(OkStatus, body, _, _) => Success(body.as[List[Urn]])
      case JsonResponse(status, _, _, _)   => errorResponse
    } handle {
      case NonFatal(e) => errorResponse
    }
  }
}

object GobblySystem {
  val agent = new Urn("soundcloud:systems:187774")
}
