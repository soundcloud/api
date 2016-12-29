package com.soundcloud.publicApiStrangler.client.reposts

import au.com.dius.pact.consumer.{PactSpec, UnitSpecsSupport}
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.test.InMemoryConfig
import com.soundcloud.jvmkit.{ResourceName, Urn, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.PactHelper
import com.soundcloud.scalakit.finagle.dns.ServiceEntryPoint
import com.soundcloud.scalakit.finagle.jsonservice.JsonClient
import org.specs2.matcher.Scope
import org.specs2.mutable.Specification

trait RepostsClientSpec extends Specification with PactSpec with UnitSpecsSupport with PactHelper {

  override val consumer = "public-api-strangler"
  override val provider = "reposts"

  val track = Urn("soundcloud:tracks:1")
  val playlist = Urn("soundcloud:playlists:1")
  val baseUrl = "http://api.example.com"

  def user = Urn(s"soundcloud:users:1")
  lazy val session = new UserSessionBuilder().setUser(user).build()

  val RequestHeaders = Map("Sc-User" -> user.toString)
  val ResponseHeaders = Map("content-type" -> "application/json;charset=utf-8")

  lazy val repostTrackRequest = buildRequest(path = "/tracks/soundcloud:tracks:1/reposts", method = "POST", headers = RequestHeaders)
  lazy val unrepostTrackRequest = buildRequest(path = "/tracks/soundcloud:tracks:1/reposts", method = "DELETE", headers = RequestHeaders)
  lazy val repostPlaylistRequest = buildRequest(path = "/playlists/soundcloud:playlists:1/reposts", method = "POST", headers = RequestHeaders)
  lazy val unrepostPlaylistRequest = buildRequest(path = "/playlists/soundcloud:playlists:1/reposts", method = "DELETE", headers = RequestHeaders)

  trait Context extends Scope {
    val client: RepostsClient = {
      val config = new InMemoryConfig
      config.set("APP_NAME", consumer)
      config.set(s"${provider.toUpperCase}_JSONCLIENT_REQUEST_TIMEOUT_MILLIS", "5000")

      val jsonClient = JsonClient(
        ResourceName(provider),
        ServiceEntryPoint(providerConfig.url),
        config,
        new Telemetry(config)
      )
      new RepostsClient(jsonClient)
    }
  }
}
