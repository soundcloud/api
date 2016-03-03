package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.finagle.Request
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.jvmkit.policies.MonetizationModel
import com.soundcloud.scalakit.Urn

class HighTierTesting {

  private val testingHeader: String = "X-Testing-Token"
  private val testingToken: String = "f4271570d8abcb6f90104e638ae182fe"

  private val testTracks = Set(
    Urn("soundcloud", "tracks", "244217343"),
    Urn("soundcloud", "tracks", "244215452"),
    Urn("soundcloud", "tracks", "244215365"),
    Urn("soundcloud", "tracks", "244215277"),
    Urn("soundcloud", "tracks", "244215147"),
    Urn("soundcloud", "tracks", "244215010"),
    Urn("soundcloud", "tracks", "244214627"),
    Urn("soundcloud", "tracks", "236046423"),
    Urn("soundcloud", "tracks", "236046361"),
    Urn("soundcloud", "tracks", "236046283"),
    Urn("soundcloud", "tracks", "236046179"),
    Urn("soundcloud", "tracks", "234657213"),
    Urn("soundcloud", "tracks", "234656952"),
    Urn("soundcloud", "tracks", "234656640")
  )

  def isEnabled(monetizationModel: MonetizationModel, session: UserSession, request: Request): Boolean = {
    monetizationModel == MonetizationModel.NOT_APPLICABLE &&
      isTrackWhiteListed(request) &&
      (session.getFeatures.contains("internal_qa") ||
        hasCorrectTestHeaderValue(request))
  }

  private def hasCorrectTestHeaderValue(request: Request): Boolean = {
    request.headerMap.get(testingHeader).filter(_ == testingToken).isDefined
  }

  private def isTrackWhiteListed(request: Request): Boolean = {
    val trackId = request.params.get("trackId")
    trackId.map(Urn("soundcloud", "tracks", _)).filter(testTracks.contains(_)).isDefined
  }


}
