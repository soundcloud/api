package com.soundcloud.publicApiStrangler.support

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class HtmlSanitizerSpec extends Specification {

  "the sanitizer" should {

    "html encode special characters" in new Scope {

      HtmlSanitizer.sanitize("<3") ==== "&lt;3"
    }

    "sanitize script tags in html" in new Scope{
      HtmlSanitizer.sanitize("<p><script><b>foo</b></script></p> & bar") ==== "<p></p> &amp; bar"
    }

    "leave valid html unaltered" in new Scope {
      private val validHtml = "<li>valid html text</li>"
      HtmlSanitizer.sanitize(validHtml) ==== validHtml
    }

    "leave html encoded string unaltered" in new Scope {
      private val encodedHtml = "&lt;3"
      HtmlSanitizer.sanitize(encodedHtml) ==== encodedHtml
    }

    "handle non-UTF8 characters" in new Scope {
      private val nonUtf8Characters = "Foo\\xbar@soundcloud.com"
      HtmlSanitizer.sanitize(nonUtf8Characters) ==== nonUtf8Characters
    }
  }

}
