package com.soundcloud.publicApiStrangler.test.util

object SeqExtensions {
  implicit class RichSeq[A](val underlying: Seq[A]) extends AnyVal {
    def percentageSatisfying(pred: A => Boolean): Int = {
      (underlying.count(pred).toDouble * 100 / underlying.size).toInt
    }
  }
}
