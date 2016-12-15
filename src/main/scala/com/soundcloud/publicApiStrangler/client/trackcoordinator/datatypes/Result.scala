package com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes

import com.soundcloud.scalakit.finagle.http.{ForbiddenStatus, OkStatus, UnauthorizedStatus}
import com.soundcloud.scalakit.finagle.jsonservice.JsonResponse
import com.twitter.util.Future
import play.api.libs.json.{JsValue, Json, Writes}

trait Result[+A] {
  def map[B](fn: A => B): Result[B]
  def flatMap[B](fn: A => Result[B]): Result[B]
  def traverseF[B](fn: A => Future[B]): Future[Result[B]]
  def flatTraverseF[B](fn: A => Future[Result[B]]): Future[Result[B]] =
    traverseF(fn).map(_.flatten)
  def zip[B](other: Result[B]): Result[(A, B)]
  def flatten[B](implicit ev: A <:< Result[B]): Result[B]
}

case class Success[A](body: A) extends Result[A] {
  override def map[B](fn: A => B): Result[B] = Success(fn(body))
  override def flatMap[B](fn: A => Result[B]): Result[B] = fn(body)
  override def traverseF[B](fn: A => Future[B]): Future[Result[B]] =
    fn(body).map(Success.apply)
  override def zip[B](other: Result[B]): Result[(A, B)] =
    other.map { second =>
      (body, second)
    }
  override def flatten[B](implicit ev: A <:< Result[B]): Result[B] = body
}

case class Created[A](body: A) extends Result[A] {
  override def map[B](fn: A => B): Result[B] = Created(fn(body))
  override def flatMap[B](fn: A => Result[B]): Result[B] = fn(body)
  override def traverseF[B](fn: A => Future[B]): Future[Result[B]] =
    fn(body).map(Created.apply)
  override def zip[B](other: Result[B]): Result[(A, B)] =
    other.map { second =>
      (body, second)
    }
  override def flatten[B](implicit ev: A <:< Result[B]): Result[B] = body
}

case class Accepted[A](body: A) extends Result[A] {
  override def map[B](fn: A => B): Result[B] = Accepted(fn(body))
  override def flatMap[B](fn: A => Result[B]): Result[B] = fn(body)
  override def traverseF[B](fn: A => Future[B]): Future[Result[B]] =
    fn(body).map(Accepted.apply)
  override def zip[B](other: Result[B]): Result[(A, B)] =
    other.map { second =>
      (body, second)
    }
  override def flatten[B](implicit ev: A <:< Result[B]): Result[B] = body
}

case object NotFound extends Result[Nothing] {
  override def map[B](fn: Nothing => B): Result[B] = NotFound
  override def flatMap[B](fn: Nothing => Result[B]): Result[B] = NotFound
  override def traverseF[B](fn: Nothing => Future[B]): Future[Result[B]] =
    Future.value(NotFound)
  override def zip[B](other: Result[B]): Result[(Nothing, B)] = NotFound
  override def flatten[B](implicit ev: Nothing <:< Result[B]): Result[B] = NotFound
}

case class ServerError(errors: Set[Error]) extends Result[Nothing] {
  override def map[B](fn: Nothing => B): Result[B] = ServerError(errors)
  override def flatMap[B](fn: Nothing => Result[B]): Result[B] =
    ServerError(errors)
  override def traverseF[B](fn: Nothing => Future[B]): Future[Result[B]] =
    Future.value(ServerError(errors))
  override def zip[B](other: Result[B]): Result[(Nothing, B)] = other match {
    case ServerError(otherErrors) => ServerError(errors ++ otherErrors)
    case _                        => ServerError(errors)
  }
  override def flatten[B](implicit ev: Nothing <:< Result[B]): Result[B] =
    ServerError(errors)
}

object ServerError {
  val empty = new ServerError(Set.empty)

  def apply(error: Error*) = new ServerError(Set(error :_*))
}

case object Unauthorized extends Result[Nothing] {
  override def map[B](fn: Nothing => B): Result[B] = Unauthorized
  override def flatMap[B](fn: Nothing => Result[B]): Result[B] = Unauthorized
  override def traverseF[B](fn: Nothing => Future[B]): Future[Result[B]] = Future.value(Unauthorized)
  override def zip[B](other: Result[B]): Result[(Nothing, B)] = Unauthorized
  override def flatten[B](implicit ev: Nothing <:< Result[B]): Result[B] = Unauthorized
}

case class ClientError(errors: Set[Error]) extends Result[Nothing] {
  override def map[B](fn: Nothing => B): Result[B] = ClientError(errors)
  override def flatMap[B](fn: Nothing => Result[B]): Result[B] =
    ClientError(errors)
  override def traverseF[B](fn: Nothing => Future[B]): Future[Result[B]] =
    Future.value(ClientError(errors))
  override def zip[B](other: Result[B]): Result[(Nothing, B)] = other match {
    case ClientError(otherErrors) => ClientError(errors ++ otherErrors)
    case _                        => ClientError(errors)
  }
  override def flatten[B](implicit ev: Nothing <:< Result[B]): Result[B] =
    ClientError(errors)
}

object ClientError {
  val empty = new ClientError(Set.empty)

  def apply(error: Error*) = new ClientError(Set(error :_*))
}

object Result {
  def sequenceF[A](result: Result[Future[A]]): Future[Result[A]] =
    result.traverseF(identity)

  def join[A1, A2](a1: Result[A1], a2: Result[A2]): Result[(A1, A2)] =
    a1 zip a2
  def join[A1, A2, A3](a1: Result[A1], a2: Result[A2], a3: Result[A3]): Result[(A1, A2, A3)] =
    (a1 zip a2 zip a3).map { case ((a1, a2), a3) => (a1, a2, a3) }
  def join[A1, A2, A3, A4](a1: Result[A1], a2: Result[A2], a3: Result[A3], a4: Result[A4]): Result[(A1, A2, A3, A4)] =
    (a1 zip a2 zip a3 zip a4).map { case (((a1, a2), a3), a4) => (a1, a2, a3, a4) }
  def join[A1, A2, A3, A4, A5, A6, A7, A8](a1: Result[A1], a2: Result[A2], a3: Result[A3], a4: Result[A4], a5: Result[A5], a6: Result[A6], a7: Result[A7], a8: Result[A8]): Result[(A1, A2, A3, A4, A5, A6, A7, A8)] =
    (a1 zip a2 zip a3 zip a4 zip a5 zip a6 zip a7 zip a8).map { case (((((((a1, a2), a3), a4), a5), a6), a7), a8) => (a1, a2, a3, a4, a5, a6, a7, a8) }
  def join[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10](a1: Result[A1], a2: Result[A2], a3: Result[A3], a4: Result[A4], a5: Result[A5], a6: Result[A6], a7: Result[A7], a8: Result[A8], a9: Result[A9], a10: Result[A10]): Result[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10)] =
    (a1 zip a2 zip a3 zip a4 zip a5 zip a6 zip a7 zip a8 zip a9 zip a10).map { case (((((((((a1, a2), a3), a4), a5), a6), a7), a8), a9), a10) => (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10) }
  def join[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11](a1: Result[A1], a2: Result[A2], a3: Result[A3], a4: Result[A4], a5: Result[A5], a6: Result[A6], a7: Result[A7], a8: Result[A8], a9: Result[A9], a10: Result[A10], a11: Result[A11]): Result[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11)] =
    (a1 zip a2 zip a3 zip a4 zip a5 zip a6 zip a7 zip a8 zip a9 zip a10 zip a11).map { case ((((((((((a1, a2), a3), a4), a5), a6), a7), a8), a9), a10), a11) => (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11) }

  def fromJsonResponse[A](from: JsonResponse, mapFn: JsValue => A): Result[A] = from match {
    case JsonResponse(OkStatus, json, _, _) => Success(mapFn(json))
    case JsonResponse(UnauthorizedStatus, json, _, _) => ClientError(Set(Error("Unauthorized")))
    case JsonResponse(ForbiddenStatus, json, _, _) => ClientError(Set(Error("Forbidden")))
    case otherwise => ServerError(Error("Unknown error"))
  }
}

case class Error(message: String) extends AnyVal
