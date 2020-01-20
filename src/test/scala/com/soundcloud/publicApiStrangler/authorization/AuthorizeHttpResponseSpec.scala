package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.authorization.policies._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.{verifyZeroInteractions, when}
import org.specs2.mutable.Before
import play.api.libs.json.{JsValue, Json}

class AuthorizeHttpResponseSpec extends UnitSpecification {
  trait Context extends Scope {
    val session = new UserSessionBuilder().build()
    val contentAuthorization = mock[ContentAuthorizationRules]
    val request = mock[HandlerRequest]
    val userAuthentication = new FakeUserAuthentication(session)

    def content: String

    def status: Status

    val authorizeContent =
      new AuthorizeHttpResponse(contentAuthorization, userAuthentication, TrackPolicyApplicator(Set[Urn]()))

    lazy val originalResponse = JsonResponseBuilder(status, content).build

    lazy val authorizedResponse = Await.result(authorizeContent.apply(request, originalResponse))
  }

  trait TrackContext extends Context with Before {
    val content = singleTrack.toString
    val status = Status.Ok
    val urn = Urn("soundcloud", "tracks", "153896632")

    def policies: ContentAuthorization

    override def before: Any = {
      when(contentAuthorization.fetchRules(session, Seq(urn))).thenReturn(Future(Seq(policies)))
    }
  }

  "renders the json track if authorized" in new TrackContext {
    lazy val policies = new ContentAuthorization(urn, ContentPolicy.ALLOW, Reason.GEO, MonetizationModel.NOT_APPLICABLE)
    lazy val authorizedTrack = singleTrack

    authorizedResponse.statusCode mustEqual 200
    authorizedResponse.getContentString mustEqual Json.stringify(authorizedTrack)
  }

  "renders forbidden if the track isn't authorized" in new TrackContext {
    lazy val policies = new ContentAuthorization(urn, ContentPolicy.BLOCK, Reason.GEO, MonetizationModel.SUB_HIGH_TIER)

    authorizedResponse.statusCode mustEqual 403
    authorizedResponse.getContentString mustEqual "{}"
  }

  trait TrackArrayContext extends Context with Before {
    val content = tracksArray.toString
    val status = Status.Ok

    def policies: Seq[ContentPolicy]

    val authorizations = tracksArray
      .as[Seq[JsValue]]
      .map(_ \ "id")
      .map(_.get)
      .map(id => Urn("soundcloud", "tracks", id.toString))
      .zip(policies)
      .map(tuple => new ContentAuthorization(tuple._1, tuple._2, Reason.GEO, MonetizationModel.NOT_APPLICABLE))

    override def before: Any = {
      when(contentAuthorization.fetchRules(===(session), any[Seq[Urn]]))
        .thenReturn(Future(authorizations))
    }
  }

  "renders an empty list if all tracks aren't authorized" in new TrackArrayContext {
    override def policies = Seq(ContentPolicy.BLOCK, ContentPolicy.BLOCK, ContentPolicy.BLOCK)

    authorizedResponse.statusCode mustEqual 200
    authorizedResponse.getContentString mustEqual "[]"
  }

  "trims a list if some tracks are not authorized" in new TrackArrayContext {
    override def policies = Seq(ContentPolicy.ALLOW, ContentPolicy.BLOCK, ContentPolicy.ALLOW)

    authorizedResponse.statusCode mustEqual 200
    Json.parse(authorizedResponse.getContentString()).as[Seq[JsValue]].size mustEqual 2
  }

  "returns all the tracks when snip" in new TrackArrayContext {
    override def policies = Seq(ContentPolicy.SNIP, ContentPolicy.SNIP, ContentPolicy.SNIP)

    authorizedResponse.statusCode mustEqual 200
    Json.parse(authorizedResponse.getContentString()).as[Seq[JsValue]].size mustEqual 3
  }

  "set duration to snip duration when track is a snip and the duration of the track is longer than snip" in new TrackArrayContext {
    override def policies = Seq(ContentPolicy.SNIP, ContentPolicy.SNIP, ContentPolicy.ALLOW)

    authorizedResponse.statusCode mustEqual 200

    val tracks = Json.parse(authorizedResponse.getContentString()).as[Seq[JsValue]]
    tracks.size mustEqual 3
    tracks.map { t =>
      (t \ "duration").as[Int]
    } ==== Seq(TrackDurationAction.snipDuration, 2000, 326183)
  }

  "trims a list if a track is blocked others are snip" in new TrackArrayContext {
    override def policies = Seq(ContentPolicy.SNIP, ContentPolicy.BLOCK, ContentPolicy.SNIP)

    authorizedResponse.statusCode mustEqual 200
    Json.parse(authorizedResponse.getContentString()).as[Seq[JsValue]].size mustEqual 2
  }

  trait NoTracksContext extends Context {
    lazy val content = "no tracks"
    lazy val status = Status.NotFound
  }

  "doesn't authorize content if there aren't tracks to authorize" in new NoTracksContext {
    authorizedResponse.status mustEqual status
    authorizedResponse.getContentString mustEqual content

    verifyZeroInteractions(contentAuthorization)
  }

  trait PlaylistContext extends Context with Before {
    val content = playlist.toString
    val status = Status.Ok

    def policies: Seq[ContentPolicy]

    val authorizations = (playlist \ "tracks")
      .as[Seq[JsValue]]
      .map(_ \ "id")
      .map(_.get)
      .map(id => Urn("soundcloud", "tracks", id.toString))
      .zip(policies)
      .map(tuple => new ContentAuthorization(tuple._1, tuple._2, Reason.GEO, MonetizationModel.NOT_APPLICABLE))

    override def before: Any =
      when(contentAuthorization.fetchRules(===(session), any[Seq[Urn]]))
        .thenReturn(Future(authorizations))
  }

  "renders playlist metadata without any tracks" in new PlaylistContext {
    override def policies = Seq.fill(10)(ContentPolicy.BLOCK)

    authorizedResponse.statusCode mustEqual 200
    (Json.parse(authorizedResponse.getContentString) \ "tracks").as[Seq[JsValue]].size mustEqual 0
  }

  "trims a list if some tracks are not authorized" in new PlaylistContext {
    override def policies = Seq(
      ContentPolicy.ALLOW,
      ContentPolicy.ALLOW,
      ContentPolicy.ALLOW,
      ContentPolicy.ALLOW,
      ContentPolicy.ALLOW,
      ContentPolicy.BLOCK,
      ContentPolicy.BLOCK,
      ContentPolicy.BLOCK,
      ContentPolicy.ALLOW,
      ContentPolicy.ALLOW
    )

    authorizedResponse.statusCode mustEqual 200
    (Json.parse(authorizedResponse.getContentString) \ "tracks").as[Seq[JsValue]].size mustEqual 7
  }

  trait StreamContext extends Context with Before {
    val content = stream.toString
    val status = Status.Ok

    def policies: Seq[ContentPolicy]

    val authorizations = (stream \ "collection" \\ "track")
      .map(_ \ "id")
      .map(_.get)
      .map(id => Urn("soundcloud", "tracks", id.toString))
      .zip(policies)
      .map(tuple => new ContentAuthorization(tuple._1, tuple._2, Reason.GEO, MonetizationModel.NOT_APPLICABLE))

    override def before: Any =
      when(contentAuthorization.fetchRules(===(session), any[Seq[Urn]]))
        .thenReturn(Future(authorizations))
  }

  "removes stream items completely, when track unauthorized" in new StreamContext {
    override def policies = Seq(
      ContentPolicy.ALLOW,
      ContentPolicy.BLOCK
    )

    authorizedResponse.statusCode mustEqual 200

    Json.parse(authorizedResponse.getContentString()) ==== streamFiltered
  }

  trait GenericContext extends Context with Before {
    val content = generic.toString
    val status = Status.Ok

    val authorizations = Seq(
      new ContentAuthorization(
        Urn("soundcloud", "tracks", "1"),
        ContentPolicy.BLOCK,
        Reason.GEO,
        MonetizationModel.NOT_APPLICABLE
      ),
      new ContentAuthorization(
        Urn("soundcloud", "tracks", "2"),
        ContentPolicy.ALLOW,
        Reason.GEO,
        MonetizationModel.NOT_APPLICABLE
      ),
      new ContentAuthorization(
        Urn("soundcloud", "tracks", "3"),
        ContentPolicy.ALLOW,
        Reason.GEO,
        MonetizationModel.NOT_APPLICABLE
      )
    )

    override def before: Any =
      when(contentAuthorization.fetchRules(===(session), any[Seq[Urn]]))
        .thenReturn(Future(authorizations))
  }

  "removes unauthorized tracks and decorates authorized with policies in various places in a  payload" in new GenericContext {
    authorizedResponse.statusCode mustEqual 200

    Json.parse(authorizedResponse.getContentString()) ==== genericFiltered
  }
}
