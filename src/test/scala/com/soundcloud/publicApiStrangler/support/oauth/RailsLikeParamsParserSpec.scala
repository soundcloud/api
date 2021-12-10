package com.soundcloud.publicApiStrangler.support.oauth

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{ParamMap, Request, RequestBuilder}
import com.twitter.io.Buf

class RailsLikeParamsParserSpec extends UnitSpecification {
  "parsing request parameters should" >> {
    trait Context extends Scope {
      val request: Request

      lazy val params: Option[Map[String, Seq[String]]] = new RailsLikeParamsParser()
        .parse(HandlerRequest(request))
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

        params ==== Some(Map("a" -> Seq("b")))
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

        params ==== Some(Map("a" -> Seq("b"), "a2" -> Seq("1"), "a3" -> Seq("2"), "a4" -> Seq("true")))
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

        params ==== Some(Map("a" -> Seq("b"), "a2" -> Seq("1")))
      }

      "prefer the last value if a parameter is passed multiple times" in new JsonContext {
        override val json =
          """
            |{
            |  "a": "b2"
            |}
            |""".stripMargin

        params ==== Some(Map("a" -> Seq("b2")))
      }

      "invalid body fails parsing" in new JsonContext {
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

      "return query parameters" in new FormUploadContext {
        override val fields = Seq.empty

        params ==== Some(Map("a" -> Seq("b")))
      }

      "return regular request parameters" in new FormUploadContext {
        override val fields = Seq(("c", "d"))

        params ==== Some(Map("a" -> Seq("b"), "c" -> Seq("d")))
      }

      "prefer the last value if a parameter is passed multiple times" in new FormUploadContext {
        override val fields = Seq(("a", "b2"))

        params ==== Some(Map("a" -> Seq("b")))
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

      "return query parameters when body is empty" in new MultipartContext {
        override val fields = Seq.empty
        params ==== Some(Map("a" -> Seq("b")))
      }

      "return additional parameters from the request body" in new MultipartContext {
        override val fields = Seq(("a2", "c"), ("a3", "d"))

        params ==== Some(Map("a" -> Seq("b"), "a2" -> Seq("c"), "a3" -> Seq("d")))
      }

      "return all values if a parameter is passed multiple times" in new MultipartContext {
        override val fields = Seq(("a2", "c"), ("a2", "d"))

        params ==== Some(Map("a" -> Seq("b"), "a2" -> Seq("c", "d")))
      }

      "invalid body does not fail parsing" in new Context {
        override val request = RequestBuilder()
          .url(Request.queryString("http://api/test", Map("a" -> "b")))
          .setHeader("Content-Type", "multipart/form-data")
          .buildPost(Buf.Utf8("Not multipart."))

        params ==== Some(Map("a" -> Seq("b")))
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
          Map(
            "a" -> Seq("b"),
            "client_id" -> Seq("multi_id"),
            "client_secret" -> Seq("multi_secret"),
            "grant_type" -> Seq("auth_code")
          )
        )
      }
    }

    "when the request Content-Type is unspecified" >> {
      "return query parameters" in new Context {
        override val request = RequestBuilder()
          .url(Request.queryString("http://api/test", Map("a" -> "b")))
          .buildPost(Buf.Empty)

        params ==== Some(Map("a" -> Seq("b")))
      }

      "prefer the last value if a parameter is passed multiple times" in new Context {
        override val request = RequestBuilder()
          .url(Request.queryString("http://api/test", Map("a" -> "b", "a" -> "c")))
          .buildPost(Buf.Empty)

        params ==== Some(Map("a" -> Seq("c")))
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
          Map("client_id" -> Seq("test_client"), "client_secret" -> Seq("test_secret"))
        )
      }

      "invalid credentials fails parsing" in new BasicAuthContext {
        override lazy val encodedString: String = "dGVzdF9jbGllbnQ6"
        override lazy val requestParams = ParamMap("a" -> "b")
        params ==== None
      }

      "invalid base64-encoded string fails parsing" in new BasicAuthContext {
        override lazy val encodedString: String = "invalid_encode_string"
        override lazy val requestParams = ParamMap("a" -> "b")
        params ==== None
      }

      "prefer the header parameters to requestParams" in new BasicAuthContext {
        override lazy val requestParams: ParamMap =
          ParamMap("client_id" -> "request_client", "client_secret" -> "request_secret")

        params ==== Some(
          Map("client_id" -> Seq("test_client"), "client_secret" -> Seq("test_secret"))
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
