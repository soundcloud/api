package com.soundcloud.apipublic.support

import org.jsoup.parser.Parser

object HtmlSanitizer {
  def sanitize(htmlString: String) = {
    Parser.unescapeEntities(htmlString, true)
  }
}
