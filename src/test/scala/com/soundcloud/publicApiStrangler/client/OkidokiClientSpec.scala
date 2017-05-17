package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.mapper._
import com.soundcloud.publicApiStrangler.representation._
import com.soundcloud.publicApiStrangler.representation.spotlight.Spotlight
import com.soundcloud.publicApiStrangler.request.representation.{EmailCreate, EmailUpdate, TranscodingCreate}
import com.soundcloud.publicApiStrangler.test.Helpers._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.{Method, Status}
import com.twitter.util.Await
import play.api.libs.json._

class OkidokiClientSpec extends UnitSpecification {

  trait Context extends Scope {
    implicit val service = mock[JsonClient]
    implicit val session = mock[UserSession]

    val addToPlaylistResponseMapper = mock[AddToPlaylistResponseMapper]
    val deleteFromPlaylistResponseMapper = mock[DeleteFromPlaylistResponseMapper]
    val createPlaylistResponseMapper = mock[CreatePlaylistResponseMapper]

    val client = new OkidokiClient(service, addToPlaylistResponseMapper, deleteFromPlaylistResponseMapper, createPlaylistResponseMapper)

    val emailFixture = Email(
      self = Self(Urn("soundcloud:emails:111"), "http://moshimoshi.int.s-cloud.net/users/soundcloud:users:49416/emails/soundcloud:emails:111"),
      address = Some("filipe@soundcloud.com"),
      bounced = Some(false),
      confirmed = Some(false),
      confirmed_at = None,
      created_at = Some("2008/12/21 17:10:34 +0000"),
      primary = Some(true)
    )
    val emailFixture2 = Email(
      self = Self(Urn("soundcloud:emails:222"), "http://moshimoshi.int.s-cloud.net/users/soundcloud:users:49416/emails/soundcloud:emails:222"),
      address = Some("marci@soundcloud.com"),
      bounced = Some(true),
      confirmed = Some(true),
      confirmed_at = Some("2008/12/21 17:10:34 +0000"),
      created_at = Some("2008/12/21 17:10:34 +0000"),
      primary = Some(false)
    )

    def expectResponseForEndpoint(path: Path, method: Method)
                                 (requestParams: Params, requestBody: JsValue)
                                 (status: Status, responseBody: JsValue = JsNull) = {
      val optionalRequestBody = requestBody match {
        case JsNull => None
        case x => Some(x)
      }

      expectResponse(
        path,
        requestParams,
        method,
        Headers.empty,
        status,
        ExpectedBody(responseBody, optionalRequestBody)
      )
    }
  }

  "#fetch" >> {

    trait UsersContext extends Context {
      val urns = Set(Urn("soundcloud:users:1"), Urn("soundcloud:tracks:1"), Urn("soundcloud:playlists:3"))

      def batchSize = 10

      def path = Path() / "fetch"

      def fetch = Await.result(client.fetch(session, urns, batchSize))

      def expectFetchResponse = expectResponseForEndpoint(path, Method.Get)(_: Params, JsNull)(_: Status, _: JsValue)
    }

    "found response" >> {
      "gotta fetch'em all" in new UsersContext {
        expectFetchResponse(urns.toList, Status.Ok, okidokiThings)
        fetch ==== okidokiThings.as[List[JsObject]]
      }
    }

    "not found response" in new UsersContext {
      expectFetchResponse(urns.toList, Status.Ok, JsArray())
      fetch ==== List()
    }

    "invalid response" in new UsersContext {
      expectFetchResponse(urns.toList, Status.InternalServerError, JsNull)
      fetch must throwA[IllegalStateException]
    }

    "batch size limited" in new UsersContext {
      override def batchSize = 2

      expectFetchResponse(urns.toList.take(2), Status.Ok, okidokiThings)
      expectFetchResponse(urns.toList.drop(2), Status.Ok, okidokiThings)
      fetch ==== okidokiThings.as[List[JsObject]] ++ okidokiThings.as[List[JsObject]]
    }
  }

  "#playlistTracks" >> {

    trait TestContext extends Context {
      val urn = Urn("soundcloud:playlists:1")
      val path = Path() / "playlists" / urn / "tracks_with_pagination"

      def expectPlaylistTracksResponse = expectResponseForEndpoint(path, Method.Get)(_: Params, JsNull)(_: Status, _: JsValue)
    }

    "invalid response" in new TestContext {
      expectPlaylistTracksResponse(Params.empty, Status.InternalServerError, JsNull)
      Await.result(client.playlistTracks(session, urn)) must throwA[IllegalStateException]
    }

    "valid response" in new TestContext {
      val afterParam = 3300
      val limit = 5
      expectPlaylistTracksResponse(Params("after" -> afterParam.toString, "limit" -> limit.toString), Status.Ok, moshiPlaylistTracksWithPagination)

      val actual = Await.result(client.playlistTracks(session, urn, Some(limit), Some(afterParam)))
      val expected = TracksWithPaginationMapper(moshiPlaylistTracksWithPagination)

      actual.meta ==== expected.meta
      actual.tracks must haveSize(1)
      actual.tracks.head.urn ==== expected.tracks.head.urn
      actual.tracks.head.duration ==== expected.tracks.head.duration
    }
  }

  "resolve by permalink" >> {

    trait ResolveContext extends Context {
      val permalink = "http://soundcloud.com/blah"
      val params = Params("permalink_url" -> permalink)
      val response = okidokiThings.as[List[JsObject]].head
      val path = Path("/resolve")

      def expectResolveResponse = expectResponseForEndpoint(path, Method.Get)(params, JsNull)(_, _)
    }

    "invalid response" in new ResolveContext {
      expectResolveResponse(Status.InternalServerError, JsNull)
      Await.result(client.resolve(session, permalink)) must throwAn[IllegalStateException]
    }

    "found response" in new ResolveContext {
      expectResolveResponse(Status.Ok, response)
      Await.result(client.resolve(session, permalink)) ==== Some(response)
    }

    "not found response" in new ResolveContext {
      expectResolveResponse(Status.Ok, JsNull)
      Await.result(client.resolve(session, permalink)) ==== None
    }
  }

  "fetches user emails" >> {

    trait EmailsContext extends Context {
      val urn = Urn("soundcloud:users:49416")
      val path = Path() / "users" / urn / "emails"

      def expectUserEmailsResponse = expectResponseForEndpoint(path, Method.Get)(Params.empty, JsNull)(_, _)
    }

    "invalid response" in new EmailsContext {
      expectUserEmailsResponse(Status.InternalServerError, JsNull)
      Await.result(client.fetchEmails(session, urn)) must throwAn[IllegalStateException]
    }

    "found response" in new EmailsContext {
      expectUserEmailsResponse(Status.Ok, okidokiUserEmails)
      Await.result(client.fetchEmails(session, urn)) ==== List(emailFixture, emailFixture2)
    }
  }

  "creates user emails" >> {

    trait CreateEmailContext extends Context {
      val userUrn = Urn("soundcloud:users:1")
      val requestBody = Json.obj("address" -> "user@example.com")
      val path = Path() / "users" / userUrn / "emails"

      def expectCreateEmailResponse = expectResponseForEndpoint(path, Method.Post)(Params.empty, requestBody)(_, _)
    }

    "invalid response" in new CreateEmailContext {
      expectCreateEmailResponse(Status.InternalServerError, JsNull)
      Await.result(client.createEmail(session, userUrn, requestBody.as[EmailCreate])) must throwA[IllegalStateException]
    }

    "invalid request: unprocessable entity" in new CreateEmailContext {
      expectCreateEmailResponse(Status.UnprocessableEntity, JsNull)
      Await.result(client.createEmail(session, userUrn, requestBody.as[EmailCreate])) ==== BadRequest(Seq.empty)
    }

    "invalid request: bad request" in new CreateEmailContext {
      expectCreateEmailResponse(Status.BadRequest, JsNull)
      Await.result(client.createEmail(session, userUrn, requestBody.as[EmailCreate])) ==== BadRequest(Seq.empty)
    }

    "invalid request: conflict" in new CreateEmailContext {
      expectCreateEmailResponse(Status.Conflict, JsNull)
      Await.result(client.createEmail(session, userUrn, requestBody.as[EmailCreate])) ==== BadRequest(Seq(Error("An email associated with that address already exists", None)))
    }

    "success response" in new CreateEmailContext {
      expectCreateEmailResponse(Status.Created, okidokiUserEmail)
      Await.result(client.createEmail(session, userUrn, requestBody.as[EmailCreate])) ==== Success(emailFixture)
    }
  }

  "updates user emails" >> {

    trait UpdateEmailContext extends Context {
      val userUrn = Urn("soundcloud:users:1")
      val emailUrn = Urn("soundcloud:emails:2")

      val path = Path() / "users" / userUrn / "emails" / emailUrn
      val requestBody = Json.obj("primary" -> true)

      def expectUpdateEmailResponse = expectResponseForEndpoint(path, Method.Put)(Params.empty, requestBody)(_, _)
    }

    "invalid response" in new UpdateEmailContext {
      expectUpdateEmailResponse(Status.InternalServerError, JsNull)
      Await.result(client.updateEmail(session, userUrn, emailUrn, requestBody.as[EmailUpdate])) must throwA[IllegalStateException]
    }

    "success response" in new UpdateEmailContext {
      expectUpdateEmailResponse(Status.Ok, okidokiUserEmail)
      Await.result(client.updateEmail(session, userUrn, emailUrn, requestBody.as[EmailUpdate])) ==== Success(emailFixture)
    }
  }

  "deletes user emails" >> {

    trait DeleteEmailContext extends Context {
      val userUrn = Urn("soundcloud:users:1")
      val emailUrn = Urn("soundcloud:emails:2")
      val path = Path() / "users" / userUrn / "emails" / emailUrn

      def expectDeleteEmailResponse = expectResponseForEndpoint(path, Method.Delete)(Params.empty, JsNull)(_, _)
    }

    "invalid response" in new DeleteEmailContext {
      expectDeleteEmailResponse(Status.InternalServerError, JsNull)
      Await.result(client.deleteEmail(session, userUrn, emailUrn)) must throwAn[IllegalStateException]
    }

    "success response" in new DeleteEmailContext {
      expectDeleteEmailResponse(Status.Ok, JsNull)
      Await.result(client.deleteEmail(session, userUrn, emailUrn)) ==== ()
    }
  }

  "fetches users restrictions" >> {

    trait RestrictionsContext extends Context {
      val possibleBlocker = Urn("soundcloud:users:15")
      val possiblyBlockedUser = Urn("soundcloud:users:10")
      val path = Path() / "users" / possiblyBlockedUser.toString / "resource_restrictions" / possibleBlocker.toString
    }

    "block exists" in new RestrictionsContext {
      expectOkResponse(path, okidokiRestrictionBlock)
      Await.result(client.fetchRestriction(session, possibleBlocker, possiblyBlockedUser)) ==== Some(UserResourceRestriction("user_blocked"))
    }

    "no block exists" in new RestrictionsContext {
      expectOkResponse(path, okidokiRestrictionNone)
      Await.result(client.fetchRestriction(session, possibleBlocker, possiblyBlockedUser)) ==== None
    }
  }

  "creates a transcoding" >> {

    trait TranscodingContext extends Context {
      val transcodingCreate = TranscodingCreate("aab123")
      val jsonRequest = Some(Json.parse("""{"transcoding":{"uid":"aab123"}}"""))
      val jsonResponse = Json.parse("""{"status":"queued"}""")
      val path = Path() / "transcodings"

      def expect(status: Status) = {
        expectResponse(
          path,
          Params.empty,
          Method.Post,
          Headers.empty,
          status,
          new ExpectedBody(jsonResponse, jsonRequest))
      }
    }

    "ok response" in new TranscodingContext {
      expect(Status.Ok)
      Await.result(client.createTranscoding(session, transcodingCreate)) ==== Success(TranscodingResponse("queued"))
    }

    "bad request" in new TranscodingContext {
      expect(Status.BadRequest)
      Await.result(client.createTranscoding(session, transcodingCreate)) ==== BadRequest(Nil)
    }
  }

  "#spotlight" >> {

    trait SpotlightContext extends Context {
      val user = Urn("soundcloud:users:1")
      val path = Path() / "users" / user.getIdentifier / "spotlight"
    }

    "when response is Ok" in new SpotlightContext {
      expectOkResponse(path, okidokiSpotlightWithItems)
      Await.result(client.spotlight(session, user)) must beAnInstanceOf[Spotlight]
    }

    "when response is not Ok" in new SpotlightContext {
      expectInternalErrorResponse(path)
      Await.result(client.spotlight(session, user)) must throwAn[UnhandledResponseException]
    }
  }
}
