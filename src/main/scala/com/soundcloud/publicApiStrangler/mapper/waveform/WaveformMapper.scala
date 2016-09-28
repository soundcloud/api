package com.soundcloud.publicApiStrangler.mapper.waveform

import com.soundcloud.bff.media.WaveformUrlsRepository
import com.soundcloud.bff.nextbff.mapper.Mapper
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.jvmkit.policies.ContentPolicy
import com.soundcloud.publicApiStrangler.mapping.Waveform
import com.soundcloud.publicApiStrangler.support.mapping.{InputValidation, ObjectMapping}
import com.twitter.util.Future

class WaveformMapper(waveformUrlsRepo: WaveformUrlsRepository)
  extends Mapper[WaveformRequestParams, Waveform]
  with InputValidation[WaveformRequestParams, Waveform] {
  self =>

  override def mapNonEmptyInputs(session: UserSession, inputs: Set[WaveformRequestParams])(implicit context: MappingContext)
  : Future[Map[WaveformRequestParams, Waveform]] = {

    val inputbyUid = inputs.map(i => i.uid -> i).toMap

    waveformUrlsRepo.fetchWaveformUrlsToMap(session, inputs.map(w => w.uid -> w.contentPolicy).toMap).map {
      waveformByUid =>
        waveformByUid.map {
          case (uid, trackWaveformUrl) =>
            inputbyUid(uid) -> new ObjectMapping(trackWaveformUrl) with Waveform
        }
    }
  }
}

case class WaveformRequestParams(uid: String, contentPolicy: ContentPolicy)
