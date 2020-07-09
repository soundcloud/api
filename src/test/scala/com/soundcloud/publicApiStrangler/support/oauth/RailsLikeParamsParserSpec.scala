package com.soundcloud.publicApiStrangler.support.oauth

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Request, RequestBuilder}
import com.twitter.io.Buf

class RailsLikeParamsParserSpec extends UnitSpecification {
  "parsing request parameters should" >> {
    trait Context extends Scope {
      val request: Request

      lazy val params: Option[Map[String, String]] = new RailsLikeParamsParser().parse(HandlerRequest(request))
    }

    "when the request Content-Type is application/json" >> {
      trait JsonContext extends Context {
        val json: String

        override lazy val request = RequestBuilder()
          .url(Request.queryString("http://api/test", Map("a" -> "b")))
          .setHeader("Content-Type", "application/json")
          .buildPost(Buf.Utf8(json))
      }

      "return query parameters" in new JsonContext {
        override val json =
          """
            |{}
            |""".stripMargin

        params ==== Some(Map("a" -> "b"))
      }

      "include additional parameters from the request body" in new JsonContext {
        override val json =
          """
            |{
            |  "a2": 1,
            |  "a3": "2",
            |  "a4": true
            |}
            |""".stripMargin

        params ==== Some(Map("a" -> "b", "a2" -> "1", "a3" -> "2", "a4" -> "true"))
      }

      "exclude complex/nested parameters from the request body" in new JsonContext {
        override val json =
          """
            |{
            |  "a2": 1,
            |  "a3": [2, 3, 4],
            |  "a4": {
            |    "a5": 1
            |  }
            |}
            |""".stripMargin

        params ==== Some(Map("a" -> "b", "a2" -> "1"))
      }

      "prefer the last value if a parameter is passed multiple times" in new JsonContext {
        override val json =
          """
            |{
            |  "a": "b2"
            |}
            |""".stripMargin

        params must beSome.which(_.get("a") ==== Some("b2"))
      }

      "handle invalid bodies" in new JsonContext {
        override val json = "This ain't JSON."

        params ==== None
      }
    }

    "when the request Content-Type is application/x-www-form-urlencoded" >> {
      trait FormUploadContext extends Context {
        val fields: Seq[(String, String)]

        override lazy val request = RequestBuilder()
          .url(Request.queryString("http://api.test", Map("a" -> "b")))
          .addFormElement(fields: _*)
          .buildFormPost(false)
      }

      "return regular request parameters" in new FormUploadContext {
        override val fields = Seq(("c", "d"))

        params ==== Some(Map("a" -> "b", "c" -> "d"))
      }

      "prefer the last value if a parameter is passed multiple times" in new FormUploadContext {
        override val fields = Seq(("a", "b2"))

        params must beSome.which(_.get("a") ==== Some("b2"))
      }
    }

    "when the request Content-Type is multipart/form-data" >> {
      trait MultipartContext extends Context {
        val fields: Seq[(String, String)]

        override lazy val request = RequestBuilder()
          .url(Request.queryString("http://api/test", Map("a" -> "b")))
          .addFormElement(fields: _*)
          .buildFormPost(multipart = true)
      }

      "return query parameters" in new MultipartContext {
        override val fields = Seq.empty

        params ==== Some(Map("a" -> "b"))
      }

      "return additional parameters from the request body" in new MultipartContext {
        override val fields = Seq(("a2", "c"), ("a3", "d"))

        params ==== Some(Map("a" -> "b", "a2" -> "c", "a3" -> "d"))
      }

      "prefer the last value if a parameter is passed multiple times" in new MultipartContext {
        override val fields = Seq(("a2", "c"), ("a2", "d"))

        params ==== Some(Map("a" -> "b", "a2" -> "d"))
      }

      "handle invalid bodies" in new Context {
        override val request = RequestBuilder()
          .url(Request.queryString("http://api/test", Map("a" -> "b")))
          .setHeader("Content-Type", "multipart/form-data")
          .buildPost(Buf.Utf8("Not multipart."))

        params ==== None
      }
    }

    "when the request Content-Type is unspecified" >> {
      "return query parameters" in new Context {
        override val request = RequestBuilder()
          .url(Request.queryString("http://api/test", Map("a" -> "b")))
          .buildPost(Buf.Empty)

        params ==== Some(Map("a" -> "b"))
      }

      "prefer the last value if a parameter is passed multiple times" in new Context {
        override val request = RequestBuilder()
          .url(Request.queryString("http://api/test", Map("a" -> "b", "a" -> "c")))
          .buildPost(Buf.Empty)

        params must beSome.which(_.get("a") ==== Some("c"))
      }
    }
  }
}
