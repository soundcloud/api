package com.soundcloud.publicApiStrangler.utilities

import com.soundcloud.bff.finagle.ResponseLike
import com.twitter.util.Future

object FutureExtensions {
  implicit class FutureOption[A](val run: Future[Option[A]]) extends AnyVal {
    def flatMap[B](f: A => FutureOption[B]): FutureOption[B] = FutureOption {
      run.flatMap { opt =>
        opt.map { a =>
          f(a).run
        }.getOrElse(Future.None)
      }
    }

    def map[B](f: A => B): FutureOption[B] = FutureOption {
      run.map(_.map(f))
    }

    def foreach(f: A => Unit): Unit = {
      run.foreach(_.foreach(f))
    }

    def lift = new FutureOption(run)

    def filter(f: A => Boolean): FutureOption[A] = FutureOption {
      run.map {
        case b @ Some(a) if f(a) => b
        case _ => None
      }
    }

    def withFilter(f: A => Boolean): FutureOption[A] = filter(f)
  }

  object FutureOption {
    def value[A](a: A): FutureOption[A] = new FutureOption(Future(Some(a)))

    val Unit = FutureOption.value(())

    def sequence[A](ffo: Future[FutureOption[A]]): FutureOption[Future[A]] = {
      val fof = for {
        fo <- ffo
        f <- fo.run
      } yield f.map(Future.value)
      fof.lift
    }

    implicit def responseLike[R](implicit ev: ResponseLike[R]): ResponseLike[FutureOption[R]] = {
      new ResponseLike[FutureOption[R]] {
        def setCookie(r: FutureOption[R], key: String, value: String): Unit = r.foreach(ev.setCookie(_, key, value))
        def unauthorized: FutureOption[R] = FutureOption.value(ev.unauthorized)
        def setHeader(r: FutureOption[R], key: String, value: String): Unit = r.foreach(ev.setHeader(_, key, value))
        def setCookieIfNotExists(r: FutureOption[R], key: String, value: String): Unit = r.foreach(ev.setCookieIfNotExists(_, key, value))
        def setHeaderIfNotExists(r: FutureOption[R], key: String, value: String): Unit = r.foreach(ev.setHeaderIfNotExists(_, key, value))
      }
    }
  }

  implicit class RichOption[A](val value: Option[A]) extends AnyVal {
    def getOrElseF[B >: A](f: => Future[B]): Future[B] = {
      value match {
        case Some(x) => Future.value(x)
        case None    => f
      }
    }
  }
}
