package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCountsClient, FollowCounts}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit._
import com.soundcloud.scalakit.finagle.http.{AlwaysMatchesPathMatcher, HandlerRequest}
import com.twitter.finagle.http.Response
import com.twitter.util.{Try, Future}
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

import scala.collection.JavaConversions._

trait FollowCountsHelper {

  def userAuthentication: UserAuthentication
  def mothershipDispatcher: DispatchToMothershipHandler
  def followCountsClient: FollowCountsClient
  def useStitchForFollowCounts: () => Future[Boolean]

  def dispatchToMothershipWithFollowCounts(request: Request): Future[ResponseBuilder] = {
    useStitchForFollowCounts().flatMap {
      case false => mothershipDispatcher.dispatch(request)

      case true =>
        userAuthentication.withUserSession(request) { session =>
          mothershipDispatcher.defaultHandling(new HandlerRequest(AlwaysMatchesPathMatcher, request)).flatMap { response =>
            (for {
              _ <- if (response.getStatusCode() < 300) Some() else None
              responseJson <- Try(Json.parse(response.getContentString())).toOption
              userUrns = extractUserUrns(responseJson)
              if userUrns.nonEmpty
            } yield {
              followCountsClient.counts(session, userUrns.toSeq).map(_.map(fc => (fc.userUrn, fc)).toMap).map { followCountsMap =>
                if (followCountsMap.nonEmpty) {
                  val content = injectFollowCounts(responseJson, followCountsMap).toString
                  response.setContentString(content)
                }
                toResponseBuilder(response)
              }
            }).getOrElse(toResponseBuilder(response).toFuture)
          }
        }
    }
  }

  private def extractUserUrns(json: JsValue): Set[Urn] = {
    json \ "collection" match {
      case collection: JsArray => extractUserUrns(collection)

      case _ => json match {
        case JsArray(values) => extractUserUrnsFromList(values)
        case single: JsObject => extractUserUrnsFromList(Seq(single))
        case _ => Set.empty
      }
    }
  }

  private def extractUserUrnsFromList(values: Seq[JsValue]): Set[Urn] =
    values
      .flatMap(getUserId)
      .map(id => Urn("soundcloud", "users", id.toString))
      .toSet

  private def getUserId(jsValue: JsValue): Option[Int] =
    (jsValue \ "user" \ "id").asOpt[Int].orElse {
      (jsValue \ "kind").asOpt[String].flatMap {
        case "user" => (jsValue \ "id").asOpt[Int]
        case _ => None
      }
    }

  private def injectFollowCounts(json: JsValue, followCountsMap: Map[Urn, FollowCounts]): JsValue = {
    json \ "collection" match {
      case collection: JsArray =>
        json.as[JsObject] ++ Json.obj("collection" -> injectFollowCounts(collection, followCountsMap))

      case _ => json match {
        case JsArray(values) => JsArray(values.map(injectFollowCountsIntoObject(_, followCountsMap)))
        case single: JsObject => injectFollowCountsIntoObject(single, followCountsMap)
        case _ => json
      }
    }
  }

  private def injectFollowCountsIntoObject(jsValue: JsValue, followCountsMap: Map[Urn, FollowCounts]): JsValue = {
    jsValue \ "user" match {
      case user: JsObject =>
        jsValue.as[JsObject] ++ Json.obj("user" -> injectFollowCountsIntoObject(user, followCountsMap))

      case _ =>
        (jsValue \ "kind").asOpt[String].flatMap {
          case "user" =>
            (jsValue \ "id").asOpt[Int].flatMap { id =>
              followCountsMap.get(Urn("soundcloud", "users", id.toString)).map { followCounts =>
                jsValue.as[JsObject] ++ Json.obj(
                  "followers_count" -> followCounts.followers,
                  "followings_count" -> followCounts.followings
                )
              }
            }

          case _ => None
        }.getOrElse(jsValue)
    }
  }

  private def toResponseBuilder(response: Response): ResponseBuilder = {
    val headerMap = response.headerMap.iterator.map {
      case (key, value) => (key, value)
    }.toMap
    new ResponseBuilder()
      .status(response.statusCode)
      .body(response.getContentString())
      .headers(headerMap)
  }
}
