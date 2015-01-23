package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.media.MediaUrlsRepository
import com.soundcloud.bff.test.{ControllerSpecification, TestConfigComponent, UnitSpecification}
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper}
import com.soundcloud.publicApiStrangler.support.{DispatchToMothershipHandler, TrackStreamSnipHandler}
import com.soundcloud.service.client.GatekeeperClient
import com.twitter.finagle.Service
import org.jboss.netty.handler.codec.http.{HttpRequest, HttpResponse}

/**
 * Support for testing TrackStreamsController
 */
class ControllerSpec extends ControllerSpecification with UnitSpecification  {

  val mediaUrlsRepositoryMock = mock[MediaUrlsRepository]
  val trackStreamUrlToJsonResponseMapperMock = mock[TrackStreamJsonResponseMapper]
  val trackStreamUrlToRedirectMapperMock = mock[TrackStreamRedirectResponseMapper]
  val contentAuthorizationServiceMock = mock[ContentAuthorizationService]
  val publicApiClientMock = mock[Service[HttpRequest, HttpResponse]]
  val mothershipDispatcherMock = mock[DispatchToMothershipHandler]
  val trackStreamSnipHandlerMock = mock[TrackStreamSnipHandler]
  val gatekeeperClientMock = mock[GatekeeperClient]

  lazy val controller = new TestBffApp
    with ConfigHack
    with TrackStreamsController
    with GroupController {

    override lazy val mediaUrlsRepository = mediaUrlsRepositoryMock
    override lazy val trackStreamUrlToJsonResponseMapper = trackStreamUrlToJsonResponseMapperMock
    override lazy val trackStreamUrlToRedirectMapper = trackStreamUrlToRedirectMapperMock
    override lazy val contentAuthorizationService = contentAuthorizationServiceMock
    override lazy val publicApiClient = publicApiClientMock
    override lazy val mothershipDispatcher = mothershipDispatcherMock
    override lazy val trackStreamSnipHandler = trackStreamSnipHandlerMock
    override val gatekeeperClient = gatekeeperClientMock

    def userSession = userSessionForTest

  }

  def userSession = controller.userSession

}

// hack because we are still using the cake
trait ConfigHack extends TestConfigComponent {
  config.set("GATEKEEPER_SRV_RECORD", "dnssrv!http.api.prod.blackhole.dd.srv.int.s-cloud.net")
}
