package com.soudcloud.authorization

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.json.Json
import scala.xml.XML

class RenderContentSpec extends UnitSpecification with Fixtures {

  "renders json" in {
    val response = RenderContent[JsonTrack](singleTrack).build
    response.getContentString mustEqual Json.stringify(singleTrack)
  }

  "renders xml" >> {
    "body" in {
      val response = RenderContent[XmlTrack](singleTrackXml).build
      XML.loadString(response.getContentString) mustEqual singleTrackXml
    }
    
    "declaration" in {
      val response = RenderContent[XmlTrack](singleTrackXml).build
      response.getContentString must startWith("""<?xml version="1.0" encoding="UTF-8"?>""")
    }
  }
}
