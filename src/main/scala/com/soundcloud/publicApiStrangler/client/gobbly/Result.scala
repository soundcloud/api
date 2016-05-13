package com.soundcloud.publicApiStrangler.client.gobbly

// Lifted from Track Coordinator

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
