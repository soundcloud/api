package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.media.WaveformUrlsRepository
import com.soundcloud.publicApiStrangler.support.{OkidokiServiceComponent, MediaServiceComponent}


trait WaveformUrlsRepositoryComponent extends MediaServiceComponent with OkidokiServiceComponent {

  lazy val waveformUrlsRepo = new WaveformUrlsRepository(okidokiService, mediaService)

}
