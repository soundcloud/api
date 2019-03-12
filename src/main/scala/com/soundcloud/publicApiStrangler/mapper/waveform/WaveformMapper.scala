package com.soundcloud.publicApiStrangler.mapper.waveform

import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.ContentPolicy
import com.soundcloud.publicApiStrangler.authorization.policies.ContentPolicy.BLOCK
import com.soundcloud.publicApiStrangler.client.media.WaveformUrlsGenerator
import com.soundcloud.publicApiStrangler.support.mapping.{InputValidation, ObjectMapping}
import com.twitter.util.Future

class WaveformMapper(waveformUrlsGenerator: WaveformUrlsGenerator)
  extends Mapper[WaveformRequestParams, Waveform]
    with InputValidation[WaveformRequestParams, Waveform] {
  self =>

  override def mapNonEmptyInputs(session: UserSession, inputs: Set[WaveformRequestParams])(implicit context: MappingContext)
  : Future[Map[WaveformRequestParams, Waveform]] = {

    Future.value(
      inputs
        .filter { case WaveformRequestParams(_, contentPolicy) =>
          contentPolicy != BLOCK
        }
        .map { waveformReqParams =>
          waveformReqParams -> new ObjectMapping(waveformUrlsGenerator.fromUid(waveformReqParams.uid)) with Waveform
        }
        .toMap
    )
  }
}

case class WaveformRequestParams(uid: String, contentPolicy: ContentPolicy)
