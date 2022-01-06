package com.soundcloud.apipublic.support

import com.twitter.util.Future

object BatchingUtilities {
  def batch[A, B](batchSize: Int, arguments: Seq[A])(f: Seq[A] => Future[Seq[B]]): Future[Seq[B]] = {
    val individualResponses = Future.collect(arguments.grouped(batchSize).map(f).toSeq)
    individualResponses.map(_.flatten)
  }
}
