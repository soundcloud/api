package com.soundcloud.publicApiStrangler.rateLimiting.semanticevents

import com.soundcloud.scalakit.Urn.format
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._

case class SemanticEvent[E](payload: E, context: SemanticEventContext)

object SemanticEvent {
  implicit val dateTimeWrites: Writes[DateTime] = Writes { dt =>
    JsString(ISODateTimeFormat.dateTime().print(dt))
  }

  implicit def semanticEventWrites[E](implicit writesE: Writes[E], sub: E <:< SemanticEventPayload): Writes[SemanticEvent[E]] =
    Writes { case SemanticEvent(payload, context) =>
      Json.obj(
        "event" -> payload.eventType,
        "at" -> context.occurredAt,
        "context" -> Json.obj(
          "app" -> context.apiClientUrn,
          "source" -> context.applicationName,
          "client_key" -> context.apiClientKey
        ),
        context.handle -> payload
      )
    }
}