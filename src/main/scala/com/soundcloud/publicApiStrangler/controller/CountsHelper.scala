package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.liebling.{LieblingClient, LikesCount}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.finagle.http.{AlwaysMatchesPathMatcher, HandlerRequest}
import com.twitter.finagle.http.Response
import com.twitter.util.{Future, Try}
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

trait CountsHelper {

  def userAuthentication: UserAuthentication
  def mothershipDispatcher: DispatchToMothershipHandler
  def followCountsClient: FollowCountsClient
  def lieblingClient: LieblingClient

  def dispatchToMothershipWithFollowCounts(request: Request): Future[ResponseBuilder] = {
    userAuthentication.withUserSession(request) { session =>
      mothershipDispatcher.defaultHandling(new HandlerRequest(AlwaysMatchesPathMatcher, request)).flatMap(response => {
        (for {
           _ <- if (response.getStatusCode() < 300) Some() else None
           responseJson <- Try(Json.parse(response.getContentString())).toOption
           userUrns = extractUserUrns(responseJson)
           if userUrns.nonEmpty
        } yield {
          val followsRequest = followCountsClient.counts(session, userUrns.toSeq)
          val lieblingRequest = lieblingClient.likeCounts(session, userUrns.toSeq)

          Future.join(followsRequest, lieblingRequest).map {
            case (followCountsResponse, lieblingCountsResponse) => {
              val followCountsMap = followCountsResponse.map(count => (count.userUrn, count)).toMap
              val lieblingCountsMap = lieblingCountsResponse.map(count => (count.target_urn, count)).toMap


              val content: String = injectKeys(responseJson, (id) => {
                val userUrn = new Urn("soundcloud", "users", id.toString)
                val followCounts = followCountsMap.get(userUrn).getOrElse(FollowCounts(userUrn, 0, 0))
                val lieblingCounts = lieblingCountsMap.get(userUrn).getOrElse(LikesCount(userUrn, 0))

                List(
                  "followers_count" -> followCounts.followers,
                  "followings_count" -> followCounts.followings,
                  "public_favorites_count" -> lieblingCounts.likes_count
                )
              }).toString

              response.setContentString(content)
            }

            toResponseBuilder(response)
          }
        }).getOrElse(toResponseBuilder(response).toFuture)
      })
    }
  }

  private def injectKeys(json: JsValue, fn: Int => Seq[(String, JsValueWrapper)]): JsValue = {
    json \ "collection" match {
      case collection: JsArray =>
        json.as[JsObject] ++ Json.obj("collection" -> injectKeys(collection, fn))

      case _ => json match {
        case JsArray(values) => JsArray(values.map(injectKeysIntoObject(_, fn)))
        case single: JsObject => injectKeysIntoObject(single, fn)
        case _ => json
      }
    }

  }
  private def injectKeysIntoObject(jsValue: JsValue, fn: Int => Seq[(String, JsValueWrapper)]): JsValue = {
    jsValue \ "user" match {
      case user: JsObject =>
        jsValue.as[JsObject] ++ Json.obj("user" -> injectKeysIntoObject(user, fn))

      case _ =>
        (jsValue \ "kind").asOpt[String].flatMap {
          case "user" =>
            (jsValue \ "id").asOpt[Int].map { id =>
              val newValues = fn(id)
              jsValue.as[JsObject] ++ Json.obj(newValues:_*)
            }
          case _ => None
        }.getOrElse(jsValue)
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
      .map(id => new Urn("soundcloud", "users", id.toString))
      .toSet

  private def getUserId(jsValue: JsValue): Option[Int] =
    (jsValue \ "user" \ "id").asOpt[Int].orElse {
      (jsValue \ "kind").asOpt[String].flatMap {
        case "user" => (jsValue \ "id").asOpt[Int]
        case _ => None
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
