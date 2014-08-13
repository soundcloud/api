package com.soudcloud.authorization

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.json.Json
import scala.xml.XML

class StringifyContentSpec extends UnitSpecification with Fixtures {

  "renders json" in {
    val response = StringifyContent[JsonTrack](singleTrack)
    response mustEqual Json.stringify(singleTrack)
  }

  "renders xml" >> {
    "body" in {
      val response = StringifyContent[XmlTrack](singleTrackXml)
      response must endWith(singleTrackXml.toString)
    }
    
    "declaration" in {
      val response = StringifyContent[XmlTrack](singleTrackXml)
      response must startWith("""<?xml version="1.0" encoding="UTF-8"?> """)
    }
  }
}
