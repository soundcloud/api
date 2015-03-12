package com.soundcloud.publicApiStrangler.mapping.timeline

import com.soundcloud.bff.test.UnitSpecification

class CommentSummarySpec extends UnitSpecification {

  trait Context extends Scope {
    implicit val context = null
  }

  "#isValid" >> {
    "there is a user" >> {
      "returns true" in new Context {
        val json = withContentsOf("okidoki", "comment-with-user")
        (new CommentSummary(json, "http://test")).isValid must beTrue
      }
    }

    "there isn't a user" >> {
      "returns false" in new Context {
        val json = withContentsOf("okidoki", "comment-without-user")
        (new CommentSummary(json, "http://test")).isValid must beFalse
      }
    }
  }
}
