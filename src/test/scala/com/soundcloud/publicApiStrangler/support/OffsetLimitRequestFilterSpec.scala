package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.soundcloud.scalakit.test.{UnitSpecification, VerifiedMocks}
import com.twitter.finagle.Service
import com.twitter.finagle.http.Request
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._

class OffsetLimitRequestFilterSpec extends UnitSpecification {

  trait Context extends VerifiedMocks {
    val next = mock[Service[Request, RouterResponse]]

    val responseFromNextService = mock[RouterResponse]
    val request: Request

    val maxOffset = 200

    def offsetOverLimitRequest(path: String) = Request(path, "offset" -> (maxOffset + 1).toString)

    def offsetAtLimitRequest(path: String) = Request(path, "offset" -> maxOffset.toString)

    val paths = Seq( """/users/\d+/favorites""", """/users/\d+/likes""")

    lazy val filter = new OffsetLimitRequestFilter(paths, maxOffset)

    lazy val response = {
      when(next.apply(request))
        .thenReturn(Future.value(responseFromNextService))

      Await.result(filter(request, next))
    }
  }

  for (
    path <- Seq(
      "/users/2/favorites",
      "/users/12/favorites",
      "/users/123/likes",
      "/users/1234/likes"
    )
  ) yield {
    s"for matched path $path" >> {
      "when enabled" >> {
        "handles offset over limit" in new Context {
          val request = offsetOverLimitRequest(path)

          response.statusCode mustEqual 403
          verifyNoMoreInteractions(next)
        }

        "handles offset over limit with trailing spaces" in new Context {
          val request = Request(path, "offset" -> ((maxOffset + 1).toString + " "))

          response.statusCode mustEqual 403
          verifyNoMoreInteractions(next)
        }

        "passes through offset at limit" in new Context {
          val request = offsetAtLimitRequest(path)

          response mustEqual responseFromNextService
        }

        "passes through non-numeric offset values" in new Context {
          val request = Request(path, "offset" -> "abc")

          response mustEqual responseFromNextService
        }

        "passes through empty offset values" in new Context {
          val request = Request(path, "offset" -> "")

          response mustEqual responseFromNextService
        }
      }
    }
  }

  for (
    path <- Seq(
      "/users/2/tracks"
    )
  ) yield {
    s"for unmatched path $path" >> {
      "passes through offset over limit" in new Context {
        val request = offsetOverLimitRequest(path)

        response mustEqual responseFromNextService
      }

      "passes through offset at limit" in new Context {
        val request = offsetAtLimitRequest(path)

        response mustEqual responseFromNextService
      }
    }
  }
}
