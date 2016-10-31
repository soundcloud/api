package com.soundcloud.publicApiStrangler.support

import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import org.jsoup.safety.Whitelist


object HtmlSanitizer {

  def sanitize(htmlString: String) = {
    val sanitized = Jsoup.clean(htmlString, Whitelist.basic())
    Parser.unescapeEntities(sanitized, true)
  }

}
