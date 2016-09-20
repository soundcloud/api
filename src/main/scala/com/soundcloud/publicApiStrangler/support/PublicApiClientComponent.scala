package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.ConfigComponent
import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.DataSensitivity.NON_SENSITIVE
import com.soundcloud.jvmkit.config.{ConfigConvention, DataSensitivity}
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.scalakit.finagle.http.HttpClientBuilder
import com.soundcloud.scalakit.finagle.jsonservice.ServiceEntryPoint
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.service.RetryPolicy.RetryableWriteException
import com.twitter.util.{Throw, Try}

trait PublicApiClientComponent {
  this: ConfigComponent =>

  lazy val publicApiClient: Service[Request, Response] = {
    val telemetry = new Telemetry(config)

    val writeExceptions: PartialFunction[(Request, Try[Response]), Boolean] = {
      case (_, Throw(RetryableWriteException(_))) => true
    }

    new HttpClientBuilder(ResourceName("PUBLIC_API"),
                          ServiceEntryPoint(config.get("PUBLIC_API_SRV_RECORD", NON_SENSITIVE)),
                          config,
                          telemetry,
                          retry=Some(writeExceptions)).client

  }
}
