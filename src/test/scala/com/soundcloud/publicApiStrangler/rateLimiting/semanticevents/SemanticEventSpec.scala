package com.soundcloud.publicApiStrangler.rateLimiting.semanticevents

import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import org.joda.time.{DateTimeZone, DateTime}
import org.joda.time.format.ISODateTimeFormat
import org.specs2.mutable.Specification
import play.api.libs.json.{JsNull, Json}

class SemanticEventSpec extends UnitSpecification {

  case class SomeEventPayload(id: Int) extends SemanticEventPayload {
    def eventType: String = "some_event_payload"
  }

  implicit val writes = Json.writes[SomeEventPayload]

  "A semantic event" should {

    "be serialized to JSON according to the requirements of Semantic Events" in {
      val payload = SomeEventPayload(23)
      val epoch = new DateTime(0, DateTimeZone.UTC)
      val context = SemanticEventContext("appName", Some("clientkey"), Urn("soundcloud", "applications", "1234"), "someHandle", epoch)
      val event = SemanticEvent(payload, context)

      Json.toJson(event) ==== Json.parse("""
        |{
        |  "event":"some_event_payload",
        |  "at": "1970-01-01T00:00:00.000Z",
        |  "context":{"app":"soundcloud:applications:1234","source":"appName","client_key":"clientkey"},
        |  "someHandle":{"id":23}
        |}
        |""".stripMargin)
    }
  }
}
