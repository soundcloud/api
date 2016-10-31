package com.soundcloud.publicApiStrangler.support

import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist


object HtmlSanitizer {

  def sanitize(htmlString: String) = Jsoup.clean(htmlString, Whitelist.basic())

}
