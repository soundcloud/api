package com.soundcloud.publicApiStrangler.test.util

import java.security.SecureRandom

class NonUniformRandomDataGenerator {
  private val underlyingGenerator = new SecureRandom

  def positiveInts: Stream[Int] = {
    Stream.continually(underlyingGenerator.nextInt.abs)
  }

  private val lowerCaseAlphabetRange = 'z' - 'a' + 1
  private val lowerCaseAlphabetStart = 'a'.toInt

  def lowerCaseAlphabeticalStrings(stringLength: Int): Stream[String] = {
    def generateString =
      Seq
        .tabulate(stringLength) { _ =>
          ((underlyingGenerator.nextInt.abs % lowerCaseAlphabetRange) + lowerCaseAlphabetStart).toChar
        }
        .mkString
    Stream.continually(generateString)
  }
}

object NonUniformRandomDataGenerator extends NonUniformRandomDataGenerator
