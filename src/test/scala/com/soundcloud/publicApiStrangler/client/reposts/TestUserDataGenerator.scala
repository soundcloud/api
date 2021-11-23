package com.soundcloud.publicApiStrangler.client.reposts

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.Json

import scala.util.Random

object TestUserDataGenerator {

  lazy val repostCountResponseLarge = Json.parse("""{"counts": [""" + generateRepostCountByLimit(100) + """]}""")

  val trackRepostCounts = Json.parse("""{
                                       |  "counts": [
                                       |    {
                                       |      "urn": "soundcloud:users:1039586182",
                                       |      "count": 2
                                       |    },
                                       |    {
                                       |      "urn": "soundcloud:users:1039586185",
                                       |      "count": 2
                                       |    },
                                       |    {
                                       |      "urn": "soundcloud:users:1039586181",
                                       |      "count": 1
                                       |    }
                                       |  ]
                                       |}""".stripMargin)

  val playListRepostCounts = Json.parse("""{
                                          |  "counts": [
                                          |    {
                                          |      "urn": "soundcloud:users:1039586182",
                                          |      "count": 6
                                          |    },
                                          |    {
                                          |      "urn": "soundcloud:users:1039586185",
                                          |      "count": 2
                                          |    },
                                          |    {
                                          |      "urn": "soundcloud:users:1039586181",
                                          |      "count": 12
                                          |    }
                                          |  ]
                                          |}""".stripMargin)

  val playListRepostCountOnlyOne = Json.parse("""{
                                                |  "counts": [
                                                |    {
                                                |      "urn": "soundcloud:users:1039586182",
                                                |      "count": 6
                                                |    }
                                                |  ]
                                                |}""".stripMargin)

  def generateRandomUsersByLimit(limit: Int): Seq[Urn] =
    for {
      x <- 1 to limit
      urn = Urn("soundcloud", "users", String.valueOf(x))
    } yield urn

  def generateRepostCountByLimit(limit: Int): String = {
    (for {
      n <- 1 to limit
      x <- Seq("""{"urn": "soundcloud:users:""" + n + "\"" + ""","count":""" + Random.nextInt(10000) + """}""")
    } yield x).mkString(",")
  }
}
