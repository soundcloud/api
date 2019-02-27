package com.soundcloud.bff.nextbff.pagination

import com.soundcloud.jvmkit.module.http.client.StringParam
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.jvmkit.module.util.Urn
import com.twitter.finagle.http.Request

class PageBuilderSpec extends UnitSpecification {

  trait Context extends Scope {
    def request = Request("/stream-with-unicorns", "param" -> "var")

    val baseUrl = "http://api-v3"
    val urn = Urn("soundcloud", "users", "2")
    val builder = PageBuilder(request, baseUrl)(urn)
  }

  "builds offset based page" in new Context {
    val page = builder.buildOffsetBased()
    page.param ==== urn
    page.baseUrl ==== baseUrl
    page.path ==== request.path
    page.extraParams must beEmpty
    page.offset ==== 0
    page.limit ==== 10
  }

  "builds offset based page with standard offset value" in new Context {
    val offset = 1
    val page = builder.buildOffsetBased(1)
    page.offset ==== offset
  }

  "builds cursor based page" in new Context {
    val page = builder.buildCursorBased()
    page.param ==== urn
    page.baseUrl ==== baseUrl
    page.path ==== request.path
    page.extraParams must beEmpty
    page.cursor ==== None
    page.limit ==== 10
  }

  "builds cursor based page with standard cursor value" in new Context {
    val cursor = Some("cursor")
    val page = builder.buildCursorBased(cursor)
    page.cursor ==== cursor
  }

  "uses the request's limit" in new Context {
    override def request = Request("/stream-with-unicorns", "limit" -> "344")

    val page = builder.buildCursorBased()
    page.limit ==== 344
  }

  "uses 10 as the default limit" in new Context {
    val page = builder.buildOffsetBased()
    page.limit ==== 10
  }

  "allows to define a default limit" in new Context {
    val defaultLimit = 233
    val page = builder.defaultLimit(defaultLimit).buildOffsetBased()
    page.limit ==== defaultLimit
  }

  "allows to define a default limit and still allows a limit parameter" in new Context {
    override def request = Request("/stream-with-unicorns", "limit" -> "344")

    val defaultLimit = 233
    val page = builder.defaultLimit(defaultLimit).buildOffsetBased()
    page.limit ==== 344
  }

  "allows to define allowed extra params" in new Context {
    val page = builder.allowExtraParams(Set("param")).buildOffsetBased()
    page.extraParams ==== Map("param" -> StringParam("var"))
  }
}
