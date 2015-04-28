package com.soundcloud.publicApiStrangler.test.data

import com.soundcloud.publicApiStrangler.rateLimiting.semanticevents.{SemanticEventPayload, SemanticEvent, SemanticEventContext}
import com.soundcloud.scalakit.Urn
import org.joda.time.{DateTimeZone, DateTime}
import play.api.libs.json.Json

trait SemanticEventTestData {

  case class SomeEventPayload(id: Int) extends SemanticEventPayload {
    def eventType: String = "some_event_payload"
  }

  implicit val writes = Json.writes[SomeEventPayload]

  val payload = SomeEventPayload(23)
  val epoch = new DateTime(0, DateTimeZone.UTC)
  val context = SemanticEventContext("appName", Some("clientkey"), Urn("soundcloud", "applications", "1234"), "someHandle", epoch)
  val event = SemanticEvent(payload, context)

  val eventJson = Json.parse("""
    |{
    |  "event":"some_event_payload",
    |  "at": "1970-01-01T00:00:00.000Z",
    |  "context":{"app":"soundcloud:applications:1234","source":"appName","client_key":"clientkey"},
    |  "someHandle":{"id":23}
    |}
    |""".stripMargin)

}
