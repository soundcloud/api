package com.soundcloud.publicApiStrangler.test

import com.soundcloud.jvmkit.module.http.server.{Handler, HandlerRequest}
import com.twitter.finagle.http._
import com.twitter.util.Await

class HandlerSpecification extends UnitSpecification {

  def get(handler: Handler,
          endpoint: String,
          routeParams: Map[String, String] = Map.empty,
          params: Map[String, String] = Map.empty,
          headers: Map[String, String] = Map.empty,
          body: String = ""): Response = execute(Method.Get, handler, endpoint, routeParams, params, headers, body)

  def delete(handler: Handler,
             endpoint: String,
             routeParams: Map[String, String] = Map.empty,
             params: Map[String, String] = Map.empty,
             headers: Map[String, String] = Map.empty,
             body: String = ""): Response = execute(Method.Delete, handler, endpoint, routeParams, params, headers, body)

  def post(handler: Handler,
           endpoint: String,
           routeParams: Map[String, String] = Map.empty,
           params: Map[String, String] = Map.empty,
           headers: Map[String, String] = Map.empty,
           body: String = ""): Response = execute(Method.Post, handler, endpoint, routeParams, params, headers, body)

  def put(handler: Handler,
          endpoint: String,
          routeParams: Map[String, String] = Map.empty,
          params: Map[String, String] = Map.empty,
          headers: Map[String, String] = Map.empty,
          body: String = ""): Response = execute(Method.Put, handler, endpoint, routeParams, params, headers, body)

  def head(handler: Handler,
           endpoint: String,
           routeParams: Map[String, String] = Map.empty,
           params: Map[String, String] = Map.empty,
           headers: Map[String, String] = Map.empty,
           body: String = ""): Response = execute(Method.Head, handler, endpoint, routeParams, params, headers, body)

  def execute(method: Method,
              handler: Handler,
              endpoint: String,
              routeParams: Map[String, String] = Map.empty,
              params: Map[String, String] = Map.empty,
              headers: Map[String, String] = Map.empty,
              body: String = ""): Response = {
    // TODO encode method, endpoint?
    Await.result(handler(createRequest(routeParams, params, headers, body)))
  }

  private def createRequest(routeParams: Map[String, String] = Map.empty,
                            params: Map[String, String] = Map.empty,
                            headers: Map[String, String] = Map.empty,
                            body: String = ""): HandlerRequest = {

    val finagleRequest = Request(params.toList: _*)
    headers.foreach { case (key, value) => finagleRequest.headerMap.set(key, value) }
    finagleRequest.setContentString(body)

    new HandlerRequest(ParamMap(routeParams), finagleRequest)
  }

}
