package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff
import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.finagle.{Request => BffRequest}
import com.soundcloud.bff.media.{TrackWaveformUrl, WaveformUrlsRepository}
import com.soundcloud.bff.nextbff.test.FakeUserAuthentication
import com.soundcloud.bff.security.AuthenticatorService
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.policies.{ContentAuthorization, ContentPolicy, Reason}
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit.json.Json
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.scalakit.{Url, Urn, UserSession}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.verifyZeroInteractions

class AuthorizeHttpResponseSpec extends UnitSpecification with Fixtures {

  trait Context extends VerifiedMocks {
    val session = mock[UserSession]
    val contentAuthorization = mock[ContentAuthorizationService]
    val waveformUrlsRepo = mock[WaveformUrlsRepository]
    val request = mock[BffRequest]
    val userAuthentication = new FakeUserAuthentication(mock[AuthenticatorService])(session)

    def content: String

    def status: Int

    val authorizeContent = new AuthorizeHttpResponse(contentAuthorization, userAuthentication, waveformUrlsRepo)

    lazy val authorize = authorizeContent.apply(request, status, content)
    lazy val authorizedResponse = Await.result(authorize.map(_.build))
  }

  trait TrackContext extends Context {
    val content = singleTrack.toString
    val status = 200
    val urn = Urn("soundcloud:tracks:153896632")

    def policies: ContentPolicy

    override def before = {
      when(contentAuthorization.findRulesApplicableTo(session, Seq(urn)))
        .thenReturn(Future(Seq(new ContentAuthorization(urn, policies, Reason.GEO))))
    }
  }

  "renders the json track if authorized" in new TrackContext {
    lazy val policies = ContentPolicy.ALLOW
    lazy val authorizedTrack = new Track(singleTrack).withPolicies(policies)

    authorizedResponse.statusCode mustEqual 200
    authorizedResponse.getContentString mustEqual Json.stringify(authorizedTrack)
  }

  "renders forbidden if the track isn't authorized" in new TrackContext {
    lazy val policies = ContentPolicy.BLOCK

    authorizedResponse.statusCode mustEqual 403
    authorizedResponse.getContentString mustEqual ""
  }

  trait TrackArrayContext extends Context {
    val content = tracksArray.toString
    val status = 200

    def policies: Seq[ContentPolicy]

    def mockWaveFormUrlsRepoExpectations: Unit = {}

    val authorizations = tracksArray.as[Seq[bff.JsValue]]
      .map(_ \ "id")
      .map(id => Urn("soundcloud:tracks:" + id))
      .zip(policies)
      .map(tuple => new ContentAuthorization(tuple._1, tuple._2, Reason.GEO))


    override def before = {
      when(contentAuthorization.findRulesApplicableTo(===(session), any[Seq[Urn]]))
        .thenReturn(Future(authorizations))
      mockWaveFormUrlsRepoExpectations
    }
  }

  "renders an empty list if all  tracks aren't authorized" in new TrackArrayContext {
    override def policies = Seq(ContentPolicy.BLOCK, ContentPolicy.BLOCK, ContentPolicy.BLOCK)

    authorizedResponse.statusCode mustEqual 200
    authorizedResponse.getContentString mustEqual "[]"
  }

  "trims a list if some tracks are not authorized" in new TrackArrayContext {
    override def policies = Seq(ContentPolicy.ALLOW, ContentPolicy.BLOCK, ContentPolicy.ALLOW)

    authorizedResponse.statusCode mustEqual 200
    Json.fromString(authorizedResponse.getContentString()).as[Seq[bff.JsValue]].size mustEqual 2
  }



  "returns all the tracks when snip" in new TrackArrayContext {

    override def policies = Seq(ContentPolicy.SNIP, ContentPolicy.SNIP, ContentPolicy.SNIP)

    override def mockWaveFormUrlsRepoExpectations = {
      val trackWaveformUrls = Map(
        Urn("soundcloud", "tracks", "49438146") -> TrackWaveformUrl("RhJ436DPf2Vx", new Url("http://bla"), new Url("http://bla2"), "stream", None),
        Urn("soundcloud", "tracks", "49437906") -> TrackWaveformUrl("DWpqP6aFqglm", new Url("http://bla3"), new Url("http://bla4"), "stream", None),
        Urn("soundcloud", "tracks", "48031525") -> TrackWaveformUrl("sDWnMpZaIQ9Z", new Url("http://bla5"), new Url("http://bla6"), "stream", None)
      )
      waveformUrlsRepo.fetchWaveformUrls(session, authorizations.toSet) returns Future.value(trackWaveformUrls)
    }

    authorizedResponse.statusCode mustEqual 200
    Json.fromString(authorizedResponse.getContentString()).as[Seq[bff.JsValue]].size mustEqual 3
  }

  "trims a list if a track is blocked others are snip" in new TrackArrayContext {
    override def policies = Seq(ContentPolicy.SNIP, ContentPolicy.BLOCK, ContentPolicy.SNIP)

    override def mockWaveFormUrlsRepoExpectations = {
      val trackWaveformUrls = Map(
        Urn("soundcloud", "tracks", "49438146") -> TrackWaveformUrl("RhJ436DPf2Vx", new Url("http://bla"), new Url("http://bla2"), "stream", None),
        Urn("soundcloud", "tracks", "48031525") -> TrackWaveformUrl("sDWnMpZaIQ9Z", new Url("http://bla5"), new Url("http://bla6"), "stream", None)
      )
      val expectedContentAuth = Set(authorizations(0), authorizations(2))
      waveformUrlsRepo.fetchWaveformUrls(session, expectedContentAuth) returns Future.value(trackWaveformUrls)
    }

    authorizedResponse.statusCode mustEqual 200
    Json.fromString(authorizedResponse.getContentString()).as[Seq[bff.JsValue]].size mustEqual 2
  }

  trait NoTracksContext extends Context {
    lazy val content = "no tracks"
    lazy val status = 404
  }

  "doesn't authorize content if there aren't tracks to authorize" in new NoTracksContext {
    authorizedResponse.statusCode mustEqual status
    authorizedResponse.getContentString mustEqual content

    verifyZeroInteractions(contentAuthorization)
  }

  trait PlaylistContext extends Context {
    val content = playlist.toString
    val status = 200

    def policies: Seq[ContentPolicy]

    val authorizations = (playlist \ "tracks").as[Seq[bff.JsValue]]
      .map(_ \ "id")
      .map(id => Urn("soundcloud:tracks:" + id))
      .zip(policies)
      .map(tuple => new ContentAuthorization(tuple._1, tuple._2, Reason.GEO))

    override def before =
      when(contentAuthorization.findRulesApplicableTo(===(session), any[Seq[Urn]]))
        .thenReturn(Future(authorizations))
  }

  "renders playlist metadata without any tracks" in new PlaylistContext {
    override def policies = Seq.fill(10)(ContentPolicy.BLOCK)

    authorizedResponse.statusCode mustEqual 200
    (Json.fromString(authorizedResponse.getContentString) \ "tracks").as[Seq[bff.JsValue]].size mustEqual 0
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
    (Json.fromString(authorizedResponse.getContentString) \ "tracks").as[Seq[bff.JsValue]].size mustEqual 7
  }

  trait StreamContext extends Context {
    val content = stream.toString
    val status = 200

    def policies: Seq[ContentPolicy]

    val authorizations = (stream \ "collection" \\ "track")
      .map(_ \ "id")
      .map(id => Urn("soundcloud:tracks:" + id))
      .zip(policies)
      .map(tuple => new ContentAuthorization(tuple._1, tuple._2, Reason.GEO))

    override def before =
      when(contentAuthorization.findRulesApplicableTo(===(session), any[Seq[Urn]]))
        .thenReturn(Future(authorizations))
  }

  "removes stream items completely, when track unauthorized" in new StreamContext {
    override def policies = Seq(
      ContentPolicy.ALLOW,
      ContentPolicy.BLOCK
    )

    authorizedResponse.statusCode mustEqual 200

    Json.fromString(authorizedResponse.getContentString()) ==== streamFiltered
  }


  trait GenericContext extends Context {
    val content = generic.toString
    val status = 200

    val authorizations = Seq(
      new ContentAuthorization(Urn("soundcloud:tracks:1"), ContentPolicy.BLOCK, Reason.GEO),
      new ContentAuthorization(Urn("soundcloud:tracks:2"), ContentPolicy.ALLOW, Reason.GEO),
      new ContentAuthorization(Urn("soundcloud:tracks:3"), ContentPolicy.ALLOW, Reason.GEO)
    )

    override def before =
      when(contentAuthorization.findRulesApplicableTo(===(session), any[Seq[Urn]]))
        .thenReturn(Future(authorizations))
  }

  "removes unauthorized tracks and decorates authorized with policies in various places in a  payload" in new GenericContext {
    authorizedResponse.statusCode mustEqual 200

    Json.fromString(authorizedResponse.getContentString()) ==== genericFiltered
  }
}
