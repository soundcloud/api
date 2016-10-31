package com.soundcloud.publicApiStrangler.support

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class HtmlSanitizerSpec extends Specification {

  "the sanitizer" should {

    "html encode special characters" in new Scope {

      HtmlSanitizer.sanitize("<3") ==== "<3"
    }

    "sanitize script tags in html" in new Scope{
      HtmlSanitizer.sanitize("<p><script><b>foo</b></script></p> & bar") ==== "<p></p> & bar"
    }

    "leave valid html unaltered" in new Scope {
      private val validHtml = "<li>valid html text</li>"
      HtmlSanitizer.sanitize(validHtml) ==== validHtml
    }

    "unescape entities " in new Scope {
      HtmlSanitizer.sanitize("&lt;3") ==== "<3"
    }

    "handle non-UTF8 characters" in new Scope {
      private val nonUtf8Characters = "Foo\\xbar@soundcloud.com"
      HtmlSanitizer.sanitize(nonUtf8Characters) ==== nonUtf8Characters
    }
  }

}
