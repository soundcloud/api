package com.soundcloud.publicApiStrangler.support.oauth

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{ParamMap, Request, RequestBuilder}
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

      "return None given empty body" in new MultipartContext {
        override val fields = Seq.empty

        params ==== None
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

      trait MultipartBasicAuthContext extends Context {
        val fields = Seq(("client_id", "multi_id"), ("client_secret", "multi_secret"), ("grant_type", "auth_code"))

        override lazy val request = RequestBuilder()
          .url(Request.queryString("http://api/test", Map("a" -> "b")))
          .setHeader("Authorization", "Basic dGVzdF9jbGllbnQ6dGVzdF9zZWNyZXQ=")
          .addFormElement(fields: _*)
          .buildFormPost(multipart = true)
      }

      "prefer the multi-part parameters to header params" in new MultipartBasicAuthContext {
        params ==== Some(
          Map("a" -> "b", "client_id" -> "multi_id", "client_secret" -> "multi_secret", "grant_type" -> "auth_code")
        )
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

    "when Auth header is present" >> {

      trait BasicAuthContext extends Context {
        lazy val encodedString = "dGVzdF9jbGllbnQ6dGVzdF9zZWNyZXQ="
        lazy val requestParams = ParamMap()

        override lazy val request = RequestBuilder()
          .url(Request.queryString("http://api/test", requestParams))
          .setHeader("Authorization", "Basic " + encodedString)
          .buildPost(Buf.Empty)
      }

      "return parameters for client_credentials flow" in new BasicAuthContext {
        params ==== Some(
          Map("client_id" -> "test_client", "client_secret" -> "test_secret")
        )
      }

      "return None for invalid credentials format" in new BasicAuthContext {
        override lazy val encodedString: String = "dGVzdF9jbGllbnQ6"
        params ==== None
      }

      "return None for invalid base64-encoded string" in new BasicAuthContext {
        override lazy val encodedString: String = "invalid_encode_string"
        params ==== None
      }

      "prefer the header parameters to requestParams" in new BasicAuthContext {
        override lazy val requestParams: ParamMap =
          ParamMap("client_id" -> "request_client", "client_secret" -> "request_secret")

        params ==== Some(
          Map("client_id" -> "test_client", "client_secret" -> "test_secret")
        )
      }

      trait AnotherAuthContext extends Context {
        override lazy val request = RequestBuilder()
          .url(Request.queryString("http://api/test"))
          .setHeader("Authorization", "Bas SOME_OTHER_AUTH")
          .buildPost(Buf.Empty)
      }

      "return empty map if header is not Basic auth" in new AnotherAuthContext {
        params ==== Some(Map.empty)
      }

    }
  }
}
