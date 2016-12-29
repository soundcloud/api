package com.soundcloud.publicApiStrangler.client

import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.model._

import scala.collection.JavaConverters._

trait PactHelper {

  def buildRequest(path: String,
                   method: String = "GET",
                   query: String = "",
                   headers: Map[String, String] = Map(),
                   body: String = "",
                   matchers: Map[String, Map[String, String]] = Map()): Request =
    new Request(method, path, PactReader.queryStringToMap(query), headers.asJava, OptionalBody.body(body), CollectionUtils.scalaMMapToJavaMMap(matchers))

  def buildResponse(status: Int = 200,
                    headers: Map[String, String] = Map(),
                    maybeBody: Option[String] = None,
                    matchers: Map[String, Map[String, String]] = Map()): Response = {
    val optionalBody = maybeBody match {
      case Some(body) => OptionalBody.body(body)
      case None => OptionalBody.missing()
    }

    new Response(status, headers.asJava, optionalBody, CollectionUtils.scalaMMapToJavaMMap(matchers))
  }

  def buildResponse(status: Int,
                    headers: Map[String, String],
                    bodyAndMatchers: DslPart): Response =
    new Response(status, headers.asJava, OptionalBody.body(bodyAndMatchers.toString), bodyAndMatchers.getMatchers)

  def buildInteraction(description: String, maybeState: Option[String], request: Request, response: Response): RequestResponseInteraction =
    new RequestResponseInteraction(description, maybeState.orNull, request, response)

  def buildPactFragment(consumer: String, provider: String, interactions: List[RequestResponseInteraction]): PactFragment =
    new PactFragment(new Consumer(consumer), new Provider(provider), interactions)
}
