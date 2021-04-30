package com.soundcloud.publicApiStrangler.service.comments

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.comments._
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.UserRepresentation
import com.soundcloud.publicApiStrangler.client.mothership.{MoshimoshiClient, RichOkidokiClient}
import com.soundcloud.publicApiStrangler.handler.comments.CreateCommentParams
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.users.UserBuilder
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import org.specs2.mock.Mockito

class CommentServiceSpec extends UnitSpecification with Mockito {

  trait Context extends Scope {
    val okidokiClient = mock[RichOkidokiClient]
    val moshimoshiClient = mock[MoshimoshiClient]
    val moshimoshiCommentsClient = mock[MoshimoshiCommentsClient]

    val trackId = 4876
    val trackUrn = Urn("soundcloud", "tracks", trackId.toString())

    val moshimoshiResponse = Fixtures.okidokiComment

    val someClientId = "veryrealclientid"

    val pagination = OffsetBasedPagination(
      "http://api.example.com",
      s"/tracks/${trackId}/comments",
      ParamMap("filter_replies" -> "0", "threaded" -> "0", "client_id" -> someClientId),
      None,
      10
    )

    val firstCommentTime = "2017-08-23T15:25:14.154Z"
    val secondCommentTime = "2017-08-23T14:25:14.154Z"
    val thirdCommentTime = "2017-08-23T13:25:14.154Z"

    val firstCommentBody = "I am a comment. I represent opinion."
    val secondCommentBody = "Computers are useless. They can only give you answers."
    val thirdCommentBody = "Those are my principles. If you don’t like them I have others."

    private def createComment(id: Long, created_at: String, user_id: Long, timestamp: Option[Int], body: String) =
      MoshimoshiCommentsComment(
        self = MoshimoshiCommentsSelf(Urn("soundcloud", "comments", id.toString)),
        created_at = created_at,
        user = MoshimoshiCommentsCommentUser(MoshimoshiCommentsSelf(Urn("soundcloud", "users", user_id.toString()))),
        track = Urn("soundlcoud", "tracks", trackId.toString()),
        timestamp = timestamp,
        body = body
      )

    // MoshimoshiCommentsComment -> Comment
    val comments = List(
      (
        createComment(1, firstCommentTime, 20, Some(1000), firstCommentBody),
        Comment(
          1,
          firstCommentBody,
          firstCommentTime,
          Some(1000),
          trackId,
          20,
          UserBuilder.user(20)
        )
      ),
      (
        createComment(2, secondCommentTime, 21, Some(1500), secondCommentBody),
        Comment(
          2,
          secondCommentBody,
          secondCommentTime,
          Some(1500),
          trackId,
          21,
          UserBuilder.user(21)
        )
      ),
      (
        createComment(3, thirdCommentTime, 22, Some(1600), thirdCommentBody),
        Comment(
          3,
          thirdCommentBody,
          thirdCommentTime,
          Some(1600),
          trackId,
          22,
          UserBuilder.user(22)
        )
      )
    )

    val commentService = new CommentService(okidokiClient, moshimoshiClient, moshimoshiCommentsClient)

  }

  "#fetchTrackComments" >> {

    "returns a bad outcome if the track comments could not be fetched" in new Context {
      val expectedResponse = NotValid("").bad

      when(moshimoshiCommentsClient.fetchTrackComments(any, any, any)).thenReturn(Future.value(expectedResponse))

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination))

      result ==== expectedResponse
    }

    "forwards a not-found-result from the client" in new Context {
      val expectedResponse = NotFound().bad

      when(moshimoshiCommentsClient.fetchTrackComments(any, any, any)).thenReturn(Future.value(expectedResponse))

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination))

      result ==== expectedResponse
    }

    "fetches the user representations for each comment" in new Context {
      when(moshimoshiCommentsClient.fetchTrackComments(any, any, any)).thenReturn(
        Future(
          MoshimoshiCommentsPagedResponse(comments.map(_._1), None).good
        )
      )

      when(
        okidokiClient.fetchUsersMap(
          anonymousSession,
          Set(UserBuilder.urnFor(20), UserBuilder.urnFor(21), UserBuilder.urnFor(22))
        )
      ).thenReturn(
        Future.value(
          Map(
            UserBuilder.urnFor(20) -> UserBuilder.user(20),
            UserBuilder.urnFor(21) -> UserBuilder.user(21),
            UserBuilder.urnFor(22) -> UserBuilder.user(22)
          )
        )
      )

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination))

      verify(moshimoshiCommentsClient).fetchTrackComments(anonymousSession, trackUrn, pagination)

      result ==== Collection(comments.map(_._2), None).good
    }

    "filters out comments where the user could not be fetched" in new Context {
      when(moshimoshiCommentsClient.fetchTrackComments(any, any, any)).thenReturn(
        Future(
          MoshimoshiCommentsPagedResponse(comments.map(_._1), None).good
        )
      )

      val users = Set(UserBuilder.urnFor(20), UserBuilder.urnFor(21), UserBuilder.urnFor(22))
      when(okidokiClient.fetchUsersMap(anonymousSession, users)).thenReturn(
        Future.value(Map(UserBuilder.urnFor(21) -> UserBuilder.user(21)))
      )

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination))

      result ==== Collection(List(comments(1)._2), None).good
    }

    "builds the next href if the backing client returns a next_href" in new Context {
      when(moshimoshiCommentsClient.fetchTrackComments(any, any, any))
        .thenReturn(
          Future.value(MoshimoshiCommentsPagedResponse(Seq.empty, Some("http://the-next-href-of-your-dreams.com")).good)
        )

      when(okidokiClient.fetchUsersMap(anonymousSession, Set.empty))
        .thenReturn(Future(Map.empty[Urn, UserRepresentation]))

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination))

      result ==== Collection(List[Comment](), Some(pagination.nextPage.normalizedHref)).good
    }
  }

  "#createComment" >> {
    "returns Comment on success" in new Context {
      val createCommentParams = CreateCommentParams(trackUrn, "bar", Some(1000), None)
      val okidokiComment = moshimoshiResponse.as[MoshimoshiCommentsComment]
      val user = UserBuilder.user(1)

      when(moshimoshiClient.createComment(anonymousSession, createCommentParams))
        .thenReturn(Future(okidokiComment.good))
      when(okidokiClient.fetchUsersMap(anonymousSession, Set(okidokiComment.user.self.urn)))
        .thenReturn(Future(Map(okidokiComment.user.self.urn -> user)))

      val result = Await.result(commentService.createComment(anonymousSession, createCommentParams))
      result ==== Comment.fromOkidokiComment(okidokiComment, user).good
    }

    "adds secret token to comment if present" in new Context {
      val secretToken = Some("secret-token")
      val createCommentParams = CreateCommentParams(trackUrn, "bar", Some(1000), secretToken)
      val okidokiComment = moshimoshiResponse.as[MoshimoshiCommentsComment]
      val user = UserBuilder.user(1)

      when(moshimoshiClient.createComment(anonymousSession, createCommentParams))
        .thenReturn(Future(okidokiComment.good))
      when(okidokiClient.fetchUsersMap(anonymousSession, Set(okidokiComment.user.self.urn)))
        .thenReturn(Future(Map(okidokiComment.user.self.urn -> user)))

      val result = Await.result(commentService.createComment(anonymousSession, createCommentParams))
      result match {
        case Good(comment) => comment.uri ==== "https://api.soundcloud.com/comments/123?secret_token=secret-token"
        case Bad(_) => true ==== false
      }
    }

    "forwards failures" in new Context {
      val createCommentParams = CreateCommentParams(trackUrn, "bar", Some(1000), None)

      when(moshimoshiClient.createComment(anonymousSession, createCommentParams)).thenReturn(Future(NotValid("").bad))

      val result = Await.result(commentService.createComment(anonymousSession, createCommentParams))
      result ==== NotValid("").bad
    }

    "handles userservice failures" in new Context {
      val createCommentParams = CreateCommentParams(trackUrn, "bar", Some(1000), None)
      val okidokiComment = moshimoshiResponse.as[MoshimoshiCommentsComment]

      when(moshimoshiClient.createComment(anonymousSession, createCommentParams))
        .thenReturn(Future(okidokiComment.good))
      when(okidokiClient.fetchUsersMap(anonymousSession, Set(okidokiComment.user.self.urn)))
        .thenReturn(Future(Map.empty[Urn, UserRepresentation]))

      val result = Await.result(commentService.createComment(anonymousSession, createCommentParams))
      result ==== NotFound().bad
    }
  }
}
