package com.soundcloud.publicApiStrangler.support

import org.jsoup.Jsoup
import org.jsoup.nodes.Document.OutputSettings
import org.jsoup.parser.Parser
import org.jsoup.safety.Whitelist

object HtmlSanitizer {
  def sanitize(htmlString: String) = {
    val sanitized = Jsoup.clean(
      htmlString,
      "",
      Whitelist.basic().addAttributes("a", "target"),
      new OutputSettings().prettyPrint(false)
    )
    Parser.unescapeEntities(sanitized, true)
  }
}
