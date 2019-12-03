package com.soundcloud.publicApiStrangler.test

import com.soundcloud.jvmkit.module.http.server._
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util.{Await, Future}
import org.specs2.matcher.ThrownExpectations

trait HandlerSpecificationScope extends org.specs2.specification.Scope with ThrownExpectations {
  def routingDefinitions(): List[(Method, String, Handler)]

  def get(
      path: String,
      params: Map[String, String] = Map.empty,
      headers: Map[String, String] = Map.empty,
      body: String = ""
  ): Response = execute(Method.Get, path, params, headers, body)

  def delete(
      path: String,
      params: Map[String, String] = Map.empty,
      headers: Map[String, String] = Map.empty,
      body: String = ""
  ): Response = execute(Method.Delete, path, params, headers, body)

  def post(
      path: String,
      params: Map[String, String] = Map.empty,
      headers: Map[String, String] = Map.empty,
      body: String = ""
  ): Response = execute(Method.Post, path, params, headers, body)

  def put(
      path: String,
      params: Map[String, String] = Map.empty,
      headers: Map[String, String] = Map.empty,
      body: String = ""
  ): Response = execute(Method.Put, path, params, headers, body)

  def head(
      path: String,
      params: Map[String, String] = Map.empty,
      headers: Map[String, String] = Map.empty,
      body: String = ""
  ): Response = execute(Method.Head, path, params, headers, body)

  def execute(
      method: Method,
      path: String,
      params: Map[String, String] = Map.empty,
      headers: Map[String, String] = Map.empty,
      body: String = ""
  ): Response = {
    Await.result(router(createFinagleRequest(method, path, params, headers, body)))
  }

  private def createFinagleRequest(
      method: Method,
      path: String,
      params: Map[String, String],
      headers: Map[String, String],
      body: String
  ) = {
    val finagleRequest = Request(path, params.toList: _*)
    finagleRequest.method = method
    finagleRequest.setContentString(body)
    finagleRequest.setContentTypeJson()
    headers.foreach { case (key, value) => finagleRequest.headerMap.set(key, value) }
    finagleRequest
  }

  lazy val router: HandlerRouter = HandlerRouterBuilder
    .register(routingDefinitions())
    .registerFallback(request => {
      failure(
        s"No matching routing found for method ${request.method} and uri: ${request.path}\n${format(routingDefinitions())}"
      )
      Future.value(Response()) // only here to make the compiler happy. The above throws an exception.
    })
    .build

  def format(list: List[(Method, String, Handler)]) = {
    list.size match {
      case 0 => "No routes registered!"
      case _ => "Registered routes:\n" + list.mkString("\n")
    }
  }
}
