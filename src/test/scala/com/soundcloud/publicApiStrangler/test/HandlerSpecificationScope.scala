package com.soundcloud.publicApiStrangler.test

import com.soundcloud.jvmkit.module.http.server._
import com.twitter.finagle.http._
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

  def putForm(
      path: String,
      params: Map[String, String] = Map.empty,
      headers: Map[String, String] = Map.empty,
      body: Seq[(String, String)] = Seq.empty,
      isMultipart: Boolean = false,
      maybeFile: Option[FileElement] = None
  ): Response = executeForm(Method.Put, path, params, headers, body, isMultipart, maybeFile)

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

  def executeForm(
      method: Method,
      path: String,
      params: Map[String, String] = Map.empty,
      headers: Map[String, String] = Map.empty,
      body: Seq[(String, String)] = Seq.empty,
      isMultipart: Boolean,
      maybeFile: Option[FileElement]
  ): Response = {
    maybeFile match {
      case Some(file) =>
        Await.result(router(createMultipartFinagleRequestWithFile(method, path, params, headers, body, file)))
      case _ => Await.result(router(createFinagleFormRequest(method, path, params, headers, body, isMultipart)))
    }
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
    finagleRequest.host = "localhost"
    finagleRequest.setContentTypeJson()
    headers.foreach { case (key, value) => finagleRequest.headerMap.set(key, value) }
    finagleRequest
  }

  private def createFinagleFormRequest(
      method: Method,
      path: String,
      params: Map[String, String],
      headers: Map[String, String],
      body: Seq[(String, String)],
      isMultipart: Boolean
  ) = {
    val request =
      RequestBuilder
        .create()
        .url(Request.queryString(s"http://api.test${path}", params))
        .addFormElement(body: _*)
        .addHeaders(headers)
        .buildFormPost(multipart = isMultipart)

    request.method = method
    request
  }

  private def createMultipartFinagleRequestWithFile(
      method: Method,
      path: String,
      params: Map[String, String],
      headers: Map[String, String],
      body: Seq[(String, String)],
      file: FileElement
  ) = {
    val request =
      RequestBuilder
        .create()
        .url(Request.queryString(s"http://api.test${path}", params))
        .addFormElement(body: _*)
        .addHeaders(headers)
        .add(file)
        .buildFormPost(multipart = true)

    request.method = method
    request
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
