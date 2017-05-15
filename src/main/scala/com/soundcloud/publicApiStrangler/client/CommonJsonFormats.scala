package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.module.util.{MalformedUrnException, Urn}
import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}
import play.api.data.validation.ValidationError
import play.api.libs.json._

object CommonJsonFormats {

  implicit val urnFormat: Format[Urn] = new Format[Urn] {
    def reads(json: JsValue) = json.validate[String].flatMap {
      str =>
        try {
          JsSuccess(new Urn(str))
        } catch {
          case _: IllegalStateException | _: MalformedUrnException => JsError(s"""Error parsing urn "$str".""")
        }
    }

    def writes(urn: Urn): JsValue = JsString(urn.toString)
  }

  implicit val urnSeqReads: Reads[Seq[Urn]] = Reads.seq
  implicit val urnSetReads: Reads[Set[Urn]] = Reads.set
  implicit val urnListReads: Reads[List[Urn]] = Reads.list

  implicit val jodaISODateReads: Reads[org.joda.time.LocalDateTime] = new Reads[org.joda.time.LocalDateTime] {
    def reads(json: JsValue): JsResult[LocalDateTime] = json match {
      case JsString(s) => JsSuccess(new LocalDateTime(new DateTime(s, DateTimeZone.UTC)))
      case _ => JsError(Seq(JsPath() -> Seq(ValidationError("Could not parse datetime value"))))
    }
  }


}
