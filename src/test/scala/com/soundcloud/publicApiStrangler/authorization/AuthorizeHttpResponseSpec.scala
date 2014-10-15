package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.test.fixtures.Fixtures
import com.soundcloud.bff
import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.finagle.{ResponseBuilder, Request => BffRequest}
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.bff.web.UserAuthenticationComponent
import com.soundcloud.bff.{BffApp, BazookaConfigComponent, BffController}
import com.soundcloud.jvmkit.policies.{ContentAuthorization, ContentPolicy, Reason}
import com.soundcloud.scalakit.json.Json
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.scalakit.{Urn, UserSession}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.verifyZeroInteractions
import scala.xml.Utility.trim
import scala.xml.XML


class AuthorizeHttpResponseSpec extends UnitSpecification with Fixtures {

  trait Context extends VerifiedMocks {
    val session = mock[UserSession]
    val contentAuthorization = mock[ContentAuthorizationService]
    val request = mock[BffRequest]
    val userAuthentication = new BazookaConfigComponent with BffApp with BffController with UserAuthenticationComponent {
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
    def policies: ContentPolicy

    override def before =
      when(contentAuthorization.findRulesApplicableTo(session, Seq(urn)))
        .thenReturn(Future(Seq(new ContentAuthorization(urn, policies, Reason.GEO))))
  }

  "renders the json track if authorized" in new JsonTrackContext {
    lazy val policies = ContentPolicy.ALLOW
    lazy val authorizedTrackJson = new JsonTrack(singleTrack).withPolicies(policies)

    authorizedResponse.statusCode mustEqual 200
    authorizedResponse.getContentString mustEqual Json.stringify(authorizedTrackJson)
  }

  "renders forbidden if the json track isn't authorized" in new JsonTrackContext {
    lazy val policies = ContentPolicy.BLOCK

    authorizedResponse.statusCode mustEqual 403
    authorizedResponse.getContentString mustEqual ""
  }

  trait JsonTrackArrayContext extends Context {
    val content = tracksArray.toString
    val status = 200
    def policies: Seq[ContentPolicy]

    val authorizations = tracksArray.as[Seq[bff.JsValue]]
        .map(_ \ "id")
        .map(id => Urn("soundcloud:tracks:" + id))
        .zip(policies)
        .map(tuple => new ContentAuthorization(tuple._1, tuple._2, Reason.GEO))

    override def before =
      when(contentAuthorization.findRulesApplicableTo(===(session), any[Seq[Urn]]))
        .thenReturn(Future(authorizations))
  }

  "renders an empty list if all json tracks aren't authorized" in new JsonTrackArrayContext {
    override def policies = Seq(ContentPolicy.BLOCK, ContentPolicy.BLOCK, ContentPolicy.BLOCK)

    authorizedResponse.statusCode mustEqual 200
    authorizedResponse.getContentString mustEqual "[]"
  }

  "trims a list if some tracks are not authorized" in new JsonTrackArrayContext {
    override def policies = Seq(ContentPolicy.ALLOW, ContentPolicy.BLOCK, ContentPolicy.ALLOW)

    authorizedResponse.statusCode mustEqual 200
    Json.fromString(authorizedResponse.getContentString()).as[Seq[bff.JsValue]].size mustEqual 2
  }

  trait XmlTrackContext extends Context {
    val content = singleTrackXml.toString
    val status = 200
    val urn = Urn("soundcloud:tracks:153896632")
    def policies: ContentPolicy

    override def before =
      when(contentAuthorization.findRulesApplicableTo(session, Seq(urn)))
        .thenReturn(Future(Seq(new ContentAuthorization(urn, policies, Reason.GEO))))
  }

  "renders the xml track if authorized" in new XmlTrackContext {
    lazy val policies = ContentPolicy.ALLOW
    lazy val authorizedTrackXml = new XmlTrack(singleTrackXml).withPolicies(policies)

    authorizedResponse.statusCode mustEqual 200
    authorizedResponse.getContentString must endWith(authorizedTrackXml.toString)
  }

  "renders forbidden if the xml track isn't authorized" in new XmlTrackContext {
    lazy val policies = ContentPolicy.BLOCK

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

  "renders playlist metadata without any tracks" in new JsonPlaylistContext {
    override def policies = Seq.fill(10)(ContentPolicy.BLOCK)

    authorizedResponse.statusCode mustEqual 200
    (Json.fromString(authorizedResponse.getContentString) \ "tracks").as[Seq[bff.JsValue]].size mustEqual 0
  }

  "trims a list if some tracks are not authorized" in new JsonPlaylistContext {
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

  trait JsonStreamContext extends Context {
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

  "removes stream items completely, when track unauthorized" in new JsonStreamContext {
    override def policies = Seq(
      ContentPolicy.ALLOW,
      ContentPolicy.BLOCK
    )

    authorizedResponse.statusCode mustEqual 200

    Json.fromString(authorizedResponse.getContentString()) ==== streamFiltered
  }


  trait JsonGenericContext extends Context {
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

  "removes unauthorized tracks and decorates authorized with policies in various places in a JSON payload" in new JsonGenericContext {
    authorizedResponse.statusCode mustEqual 200

    Json.fromString(authorizedResponse.getContentString()) ==== genericFiltered
  }

  trait XmlStreamContext extends Context {
    val content = streamXml.toString
    val status = 200

    val authorizations = Seq(
      new ContentAuthorization(Urn("soundcloud:tracks:169183738"), ContentPolicy.ALLOW, Reason.GEO),
      new ContentAuthorization(Urn("soundcloud:tracks:168272289"), ContentPolicy.BLOCK, Reason.GEO),
      new ContentAuthorization(Urn("soundcloud:tracks:168546826"), ContentPolicy.ALLOW, Reason.GEO)
    )

    override def before = {
      when(contentAuthorization.findRulesApplicableTo(===(session), any[Seq[Urn]]))
        .thenReturn(Future(authorizations))

    }
  }

  "Stream test: Complete activity item should be removed when track is blocked." in new XmlStreamContext {

    authorizedResponse.statusCode mustEqual 200
    trim(XML.loadString(authorizedResponse.getContentString())) mustEqual trim(streamFilteredXml)

  }

  trait XmlGenericContext extends Context {
    val content = genericXml.toString
    val status = 200

    val authorizations = Seq(
      new ContentAuthorization(Urn("soundcloud:tracks:1"), ContentPolicy.BLOCK, Reason.GEO),
      new ContentAuthorization(Urn("soundcloud:tracks:2"), ContentPolicy.ALLOW, Reason.GEO),
      new ContentAuthorization(Urn("soundcloud:tracks:3"), ContentPolicy.ALLOW, Reason.GEO)
    )

    override def before = {
      when(contentAuthorization.findRulesApplicableTo(===(session), any[Seq[Urn]]))
        .thenReturn(Future(authorizations))

    }
  }

  "removes unauthorized tracks and decorates authorized with policies in various places in a XML payload" in new XmlGenericContext {

    authorizedResponse.statusCode mustEqual 200
    trim(XML.loadString(authorizedResponse.getContentString())) mustEqual trim(genericFilteredXml)

  }

}
