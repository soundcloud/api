package com.soundcloud.apipublic.support

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class HtmlSanitizerSpec extends Specification {
  "the sanitizer" should {
    "unescape entities " in new Scope {
      HtmlSanitizer.sanitize("&lt;3") ==== "<3"
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
  }
}
