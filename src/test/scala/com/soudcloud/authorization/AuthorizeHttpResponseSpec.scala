package com.soudcloud.authorization

import com.soundcloud.bff
import com.soundcloud.bff.{BazookaConfigComponent, BffController}
import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.finagle.{ResponseBuilder, Request => BffRequest}
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.bff.web.UserAuthenticationComponent
import com.soundcloud.jvmkit.policies.{ContentAuthorization, ContentPolicies, Reasons}
import com.soundcloud.scalakit.{Urn, UserSession}
import com.soundcloud.scalakit.json.Json
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.verifyZeroInteractions
import play.api.libs.json.JsString

class AuthorizeHttpResponseSpec extends UnitSpecification with Fixtures {

  trait Context extends VerifiedMocks {
    val session = mock[UserSession]
    val contentAuthorization = mock[ContentAuthorizationService]
    val request = mock[BffRequest]
    val userAuthentication = new BazookaConfigComponent with BffController with UserAuthenticationComponent {
      override val applicationName = "test"
      override def withUserSession(request: BffRequest)(action: (UserSession) => Future[ResponseBuilder]) = {
        request mustEqual Context.this.request
        action(session)
      }
    }
    def content: String
    def status: Int

    val authorizeContent = new AuthorizeHttpResponse(contentAuthorization, userAuthentication)

    lazy val authorize = authorizeContent.apply(request, status, content)
    lazy val authorizedResponse = Await.result(authorize.map(_.build))
  }

  trait JsonTrackContext extends Context {
    val content = singleTrack.toString
    val status = 200
    val urn = Urn("soundcloud:tracks:153896632")
    def policies: ContentPolicies

    override def before =
      when(contentAuthorization.findRulesApplicableTo(session, Seq(urn)))
        .thenReturn(Future(Seq(new ContentAuthorization(urn, policies, Reasons.GEO))))
  }

  "renders the json track if authorized" in new JsonTrackContext {
    lazy val policies = ContentPolicies.ALLOW
    lazy val authorizedTrackJson = new JsonTrack(singleTrack).withPolicies(policies)

    authorizedResponse.statusCode mustEqual 200
    authorizedResponse.getContentString mustEqual Json.stringify(authorizedTrackJson)
  }

  "renders forbidden if the json track isn't authorized" in new JsonTrackContext {
    lazy val policies = ContentPolicies.BLOCK

    authorizedResponse.statusCode mustEqual 403
    authorizedResponse.getContentString mustEqual ""
  }

  trait JsonTrackArrayContext extends Context {
    val content = tracksArray.toString
    val status = 200
    def policies: Seq[ContentPolicies]

    val authorizations = tracksArray.as[Seq[bff.JsValue]]
        .map(_ \ "id")
        .map(id => Urn("soundcloud:tracks:" + id))
        .zip(policies)
        .map(tuple => new ContentAuthorization(tuple._1, tuple._2, Reasons.GEO))

    override def before =
      when(contentAuthorization.findRulesApplicableTo(===(session), any[Seq[Urn]]))
        .thenReturn(Future(authorizations))
  }

  "renders an empty list if all json tracks aren't authorized" in new JsonTrackArrayContext {
    override def policies = Seq(ContentPolicies.BLOCK, ContentPolicies.BLOCK, ContentPolicies.BLOCK)

    authorizedResponse.statusCode mustEqual 200
    authorizedResponse.getContentString mustEqual "[]"
  }

  "trims a list if some tracks are not authorized" in new JsonTrackArrayContext {
    override def policies = Seq(ContentPolicies.ALLOW, ContentPolicies.BLOCK, ContentPolicies.ALLOW)

    authorizedResponse.statusCode mustEqual 200
    Json.fromJson(authorizedResponse.getContentString()).as[Seq[bff.JsValue]].size mustEqual 2
  }

  trait XmlTrackContext extends Context {
    val content = singleTrackXml.toString
    val status = 200
    val urn = Urn("soundcloud:tracks:153896632")
    def policies: ContentPolicies

    override def before =
      when(contentAuthorization.findRulesApplicableTo(session, Seq(urn)))
        .thenReturn(Future(Seq(new ContentAuthorization(urn, policies, Reasons.GEO))))
  }

  "renders the xml track if authorized" in new XmlTrackContext {
    lazy val policies = ContentPolicies.ALLOW
    lazy val authorizedTrackXml = new XmlTrack(singleTrackXml).withPolicies(policies)

    authorizedResponse.statusCode mustEqual 200
    authorizedResponse.getContentString must endWith(authorizedTrackXml.toString)
  }

  "renders forbidden if the xml track isn't authorized" in new XmlTrackContext {
    lazy val policies = ContentPolicies.BLOCK

    authorizedResponse.statusCode mustEqual 403
    authorizedResponse.getContentString mustEqual ""
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

  trait JsonPlaylistContext extends Context {
    val content = playlist.toString
    val status = 200
    def policies: Seq[ContentPolicies]

    val authorizations = (playlist \ "tracks").as[Seq[bff.JsValue]]
      .map(_ \ "id")
      .map(id => Urn("soundcloud:tracks:" + id))
      .zip(policies)
      .map(tuple => new ContentAuthorization(tuple._1, tuple._2, Reasons.GEO))

    override def before =
      when(contentAuthorization.findRulesApplicableTo(===(session), any[Seq[Urn]]))
        .thenReturn(Future(authorizations))
  }

  "renders playlist metadata without any tracks" in new JsonPlaylistContext {
    override def policies = Seq.fill(10)(ContentPolicies.BLOCK)

    authorizedResponse.statusCode mustEqual 200
    (Json.fromJson(authorizedResponse.getContentString) \ "tracks").as[Seq[bff.JsValue]].size mustEqual 0
  }

  "trims a list if some tracks are not authorized" in new JsonPlaylistContext {
    override def policies = Seq(
      ContentPolicies.ALLOW,
      ContentPolicies.ALLOW,
      ContentPolicies.ALLOW,
      ContentPolicies.ALLOW,
      ContentPolicies.ALLOW,

      ContentPolicies.BLOCK,
      ContentPolicies.BLOCK,
      ContentPolicies.BLOCK,

      ContentPolicies.ALLOW,
      ContentPolicies.ALLOW
    )

    authorizedResponse.statusCode mustEqual 200
    (Json.fromJson(authorizedResponse.getContentString) \ "tracks").as[Seq[bff.JsValue]].size mustEqual 7
  }

  trait JsonStreamContext extends Context {
    val content = stream.toString
    val status = 200
    def policies: Seq[ContentPolicies]

    val authorizations = (stream \ "collection").as[Seq[bff.JsValue]]
      .map(_ \ "track" \ "id")
      .map(id => Urn("soundcloud:tracks:" + id))
      .zip(policies)
      .map(tuple => new ContentAuthorization(tuple._1, tuple._2, Reasons.GEO))

    override def before =
      when(contentAuthorization.findRulesApplicableTo(===(session), any[Seq[Urn]]))
        .thenReturn(Future(authorizations))
  }

  "removes stream items completely, when track unauthorized" in new JsonStreamContext {
    override def policies = Seq(
      ContentPolicies.ALLOW,
      ContentPolicies.BLOCK
    )

    authorizedResponse.statusCode mustEqual 200

    Json.fromJson(authorizedResponse.getContentString()) ==== streamFiltered
  }
}
