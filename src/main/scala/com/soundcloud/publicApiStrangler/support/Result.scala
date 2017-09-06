package com.soundcloud.publicApiStrangler.support

import com.twitter.util.Future

import scala.util.{Failure, Success, Try}

sealed abstract class Result[+A] {
  def map[B](f: A => B): Result[B] = this match {
    case Good(a) => Good(f(a))
    case Bad(errors) => Bad(errors)
  }

  def flatMap[B](f: A => Result[B]): Result[B] = this match {
    case Good(a) => f(a)
    case Bad(errors) => Bad(errors)
  }

  def withFilter(f: A => Boolean): Result[A] = this match {
    case good@Good(a) if (f(a)) => good
    case Good(_) => Bad(StringError("match failed"))
    case otherwise => otherwise
  }

  def join[B](other: Result[B]): Result[(A, B)] = Result.join(this, other)

  def flatten[B](implicit ev: A <:< Result[B]): Result[B] = this match {
    case Good(a) => a
    case Bad(errors) => Bad(errors)
  }
}

object Result {
  def collect(results: Seq[Result[_]]): Result[Seq[_]] =
    results.collect { case bad@Bad(_) => bad } match {
      case Seq(firstLeft, _*) => firstLeft
      case Nil => Good(results.collect { case Good(value) => value })
    }

  def collectAll(results: Seq[Result[_]]): (Seq[Good[_]], Seq[Bad]) = {
    (
      results.collect { case good@Good(_) => good },
      results.collect { case bad@Bad(_) => bad }
    )
  }

  def join[A1, A2](a1: Result[A1], a2: Result[A2]): Result[(A1, A2)] =
    for {
      a1 <- a1
      a2 <- a2
    } yield (a1, a2)

  def join[A1, A2, A3](a1: Result[A1], a2: Result[A2], a3: Result[A3]): Result[(A1, A2, A3)] =
    join(join(a1, a2), a3).map { case ((a1, a2), a3) => (a1, a2, a3) }

  def join[A1, A2, A3, A4](a1: Result[A1], a2: Result[A2], a3: Result[A3], a4: Result[A4]): Result[(A1, A2, A3, A4)] =
    join(join(join(a1, a2), a3), a4).map { case (((a1, a2), a3), a4) => (a1, a2, a3, a4) }

  def join[A1, A2, A3, A4, A5](a1: Result[A1], a2: Result[A2], a3: Result[A3], a4: Result[A4], a5: Result[A5]): Result[(A1, A2, A3, A4, A5)] =
    join(join(join(join(a1, a2), a3), a4), a5).map { case ((((a1, a2), a3), a4), a5) => (a1, a2, a3, a4, a5) }

  def join[A1, A2, A3, A4, A5, A6](a1: Result[A1], a2: Result[A2], a3: Result[A3], a4: Result[A4], a5: Result[A5], a6: Result[A6]): Result[(A1, A2, A3, A4, A5, A6)] =
    join(join(join(join(join(a1, a2), a3), a4), a5), a6).map { case (((((a1, a2), a3), a4), a5), a6) => (a1, a2, a3, a4, a5, a6) }

  def join[A1, A2, A3, A4, A5, A6, A7](a1: Result[A1], a2: Result[A2], a3: Result[A3], a4: Result[A4], a5: Result[A5], a6: Result[A6], a7: Result[A7]): Result[(A1, A2, A3, A4, A5, A6, A7)] =
    join(join(join(join(join(join(a1, a2), a3), a4), a5), a6), a7).map { case ((((((a1, a2), a3), a4), a5), a6), a7) => (a1, a2, a3, a4, a5, a6, a7) }

  implicit class OptionToResult[T](o: Option[T]) {
    def toResult(ifNone: => ErrorLike): Result[T] = {
      o.map(Good(_)).getOrElse(Bad(ifNone))
    }
  }

  implicit class TryToResult[T](t: Try[T]) {
    def toResult: Result[T] = t match {
      case Success(v) => Good(v)
      case Failure(e) => Bad(StringError("Unexpected exception"))
    }
  }

}

case class Good[+A](a: A) extends Result[A]

case class Bad(error: ErrorLike) extends Result[Nothing]

sealed case class ResultF[+A](value: Future[Result[A]]) {

  def map[B](f: A => B): ResultF[B] =
    ResultF(value.map {
      case Good(a) => Good(f(a))
      case Bad(errors) => Bad(errors)
    })

  def flatMap[B](f: A => ResultF[B]): ResultF[B] =
    ResultF(value.flatMap {
      case Good(a) => f(a).value
      case Bad(errors) => Future.value(Bad(errors))
    })

  def join[B](other: ResultF[B]): ResultF[(A, B)] =
    ResultF.joinF(this, other)

  def withFilter(f: A => Boolean): ResultF[A] = ResultF(value.map {
    case good@Good(a) if f(a) => good
    case Good(_) => Bad(StringError(s"Match failed"))
    case otherwise => otherwise
  })

  def handle[B >: Result[A]](rescueException: PartialFunction[Throwable, B]): Future[B] = value.handle[B](rescueException)
}

object ResultF {

  def collectF(results: Seq[ResultF[_]]): ResultF[Seq[_]] = {
    val collected = Future.collect(results.map(_.value))
    ResultF(collected.map(Result.collect))
  }

  def collectAllF(results: Seq[ResultF[_]]): Future[(Seq[Good[_]], Seq[Bad])] = {
    val collected = Future.collect(results.map(_.value))
    collected.map(Result.collectAll)
  }

  def lift[A](result: Result[A]): ResultF[A] = ResultF(Future.value(result))

  def lift[A](resultF: Future[Result[A]]): ResultF[A] = ResultF(resultF)

  def joinF[A1, A2](a1: ResultF[A1], a2: ResultF[A2]): ResultF[(A1, A2)] =
    for {
      a1 <- a1
      a2 <- a2
    } yield (a1, a2)

  def joinF[A1, A2, A3](a1: ResultF[A1], a2: ResultF[A2], a3: ResultF[A3]): ResultF[(A1, A2, A3)] =
    joinF(joinF(a1, a2), a3).map { case ((a1, a2), a3) => (a1, a2, a3) }

  def joinF[A1, A2, A3, A4](a1: ResultF[A1], a2: ResultF[A2], a3: ResultF[A3], a4: ResultF[A4]): ResultF[(A1, A2, A3, A4)] =
    joinF(joinF(joinF(a1, a2), a3), a4).map { case (((a1, a2), a3), a4) => (a1, a2, a3, a4) }

  def joinF[A1, A2, A3, A4, A5](a1: ResultF[A1], a2: ResultF[A2], a3: ResultF[A3], a4: ResultF[A4], a5: ResultF[A5]): ResultF[(A1, A2, A3, A4, A5)] =
    joinF(joinF(joinF(joinF(a1, a2), a3), a4), a5).map { case ((((a1, a2), a3), a4), a5) => (a1, a2, a3, a4, a5) }

  def joinF[A1, A2](a1: Future[Result[A1]], a2: Future[Result[A2]]): ResultF[(A1, A2)] =
    ResultF(Future.join(a1, a2).map { case (a1, a2) => Result.join(a1, a2) })

  def joinF[A1, A2, A3](a1: Future[Result[A1]], a2: Future[Result[A2]], a3: Future[Result[A3]]): ResultF[(A1, A2, A3)] =
    ResultF(Future.join(a1, a2, a3).map { case (a1, a2, a3) => Result.join(a1, a2, a3) })

  def joinF[A1, A2, A3, A4](a1: Future[Result[A1]], a2: Future[Result[A2]], a3: Future[Result[A3]], a4: Future[Result[A4]]): ResultF[(A1, A2, A3, A4)] =
    ResultF(Future.join(a1, a2, a3, a4).map { case (a1, a2, a3, a4) => Result.join(a1, a2, a3, a4) })

  def joinF[A1, A2, A3, A4, A5](a1: Future[Result[A1]], a2: Future[Result[A2]], a3: Future[Result[A3]], a4: Future[Result[A4]], a5: Future[Result[A5]]): ResultF[(A1, A2, A3, A4, A5)] =
    ResultF(Future.join(a1, a2, a3, a4, a5).map { case (a1, a2, a3, a4, a5) => Result.join(a1, a2, a3, a4, a5) })

  def joinF[A1, A2, A3, A4, A5, A6](a1: Future[Result[A1]], a2: Future[Result[A2]], a3: Future[Result[A3]], a4: Future[Result[A4]], a5: Future[Result[A5]], a6: Future[Result[A6]]): ResultF[(A1, A2, A3, A4, A5, A6)] =
    ResultF(Future.join(a1, a2, a3, a4, a5, a6).map { case (a1, a2, a3, a4, a5, a6) => Result.join(a1, a2, a3, a4, a5, a6) })

  def joinF[A1, A2, A3, A4, A5, A6, A7](a1: Future[Result[A1]], a2: Future[Result[A2]], a3: Future[Result[A3]], a4: Future[Result[A4]], a5: Future[Result[A5]], a6: Future[Result[A6]], a7: Future[Result[A7]]): ResultF[(A1, A2, A3, A4, A5, A6, A7)] =
    ResultF(Future.join(a1, a2, a3, a4, a5, a6, a7).map { case (a1, a2, a3, a4, a5, a6, a7) => Result.join(a1, a2, a3, a4, a5, a6, a7) })
}

trait ErrorLike

case class StringError(message: String) extends ErrorLike