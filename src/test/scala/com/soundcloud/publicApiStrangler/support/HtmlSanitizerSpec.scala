package com.soundcloud.publicApiStrangler.support

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class HtmlSanitizerSpec extends Specification {
  "the sanitizer" should {
    "html encode special characters" in new Scope {
      HtmlSanitizer.sanitize("<3") ==== "<3"
    }

    "sanitize script tags in html" in new Scope {
      HtmlSanitizer.sanitize("<p><script><b>foo</b></script></p> & bar") ==== "<p></p> & bar"
    }

    "leave valid html unaltered" in new Scope {
      private val validHtml = "<li>valid html text</li>"
      HtmlSanitizer.sanitize(validHtml) ==== validHtml
    }

    "unescape entities " in new Scope {
      HtmlSanitizer.sanitize("&lt;3") ==== "<3"
    }

    "sanitize everything inside malformed HTML entities" in new Scope {
      HtmlSanitizer.sanitize("Foo <script> bar baz quax") ==== "Foo "
    }

    "handle non-UTF8 characters" in new Scope {
      private val nonUtf8Characters = "Foo\\xbar@soundcloud.com"
      HtmlSanitizer.sanitize(nonUtf8Characters) ==== nonUtf8Characters
    }

    "does not prune extra whitespaces" in new Scope {
      private val original = "        Foo          Bar      "
      HtmlSanitizer.sanitize(original) ==== original
    }

    "does not remove leading and trailing whitespace" in new Scope {
      HtmlSanitizer.sanitize("Foo      ") ==== "Foo      "
      HtmlSanitizer.sanitize("      Bar") ==== "      Bar"
    }

    "does not remove linebreaks" in new Scope {
      HtmlSanitizer.sanitize("Foo\n\nBar") ==== "Foo\n\nBar"
    }

    "does not remove carriage return" in new Scope {
      HtmlSanitizer.sanitize("Foo \r\r Bar") ==== "Foo \r\r Bar"
    }

    "document behaviour of anchor tags" in new Scope {
      HtmlSanitizer.sanitize("<a href=\"https://www.foo/bar\" target=\"_blank\">baz</a>") ====
        "<a href=\"https://www.foo/bar\" target=\"_blank\" rel=\"nofollow\">baz</a>"
    }
  }
}
