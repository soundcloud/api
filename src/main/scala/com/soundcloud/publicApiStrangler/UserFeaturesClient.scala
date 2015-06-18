package com.soundcloud.bff.security

import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.JsonClient
import com.soundcloud.scalakit.{Path, Urn}
import com.twitter.util.{NonFatal, Future}

class UserFeaturesClient(gatekeeper: JsonClient) {

  val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  def getFeatures(userUrn: Urn): Future[Set[String]] = {
    gatekeeper.getWithoutSession(Path() / "users" / userUrn / "features").map(
      response =>
        response.status match {
          case OkStatus => response.body.as[Seq[String]].toSet
          case _ => Set.empty[String]
        }
    ).handle {
      case NonFatal(e) =>
        logger.debug("Gatekeeper failed returning empty features")
        logger.debug(s"exception class : ${e.getClass}")
        logger.debug(s"exception; ${e.getCause} ; ${e.getMessage}")
        logger.debug(s"exception dump; $e")
        logger.debug(s"exception stacktrace; ${e.getStackTrace}")

        e.getStackTrace.foreach{
          st =>
            logger.debug(st.toString)
        }

        Set.empty[String]
    }
  }
}
