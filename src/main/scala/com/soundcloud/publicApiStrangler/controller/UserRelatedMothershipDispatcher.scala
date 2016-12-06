package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.web.UserAuthentication
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.liebling.{LieblingClient, LikesCount, UserTotalLikes}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.finagle.http.{AlwaysMatchesPathMatcher, HandlerRequest}
import com.twitter.finagle.http.Response
import com.twitter.util.{Future, Try}
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

class UserRelatedMothershipDispatcher(userAuthentication: UserAuthentication,
                                      mothershipDispatcher: DispatchToMothershipHandler,
                                      followCountsClient: FollowCountsClient,
                                      lieblingClient: LieblingClient,
                                      shouldLoadCountsFromLiebling: () => Future[Boolean]) {

  def dispatchToMothership(request: Request): Future[ResponseBuilder] = {
    userAuthentication.withUserSession(request) { session =>
      mothershipDispatcher.defaultHandling(new HandlerRequest(AlwaysMatchesPathMatcher, request)).flatMap(response => {
        (for {
          _ <- if (response.getStatusCode() < 300) Some() else None
          responseJson <- Try(Json.parse(response.getContentString())).toOption
          userUrns = extractUserUrns(responseJson)
          if userUrns.nonEmpty
        } yield {
          shouldLoadCountsFromLiebling().flatMap {
            case true => requestWithLiebling(session, userUrns, responseJson, response)
            case false => requestWithoutLiebling(session, userUrns, responseJson, response)
          }
        }).getOrElse(toResponseBuilder(response).toFuture)
      })
    }
  }

  // TODO remove after feature flag is removed
  private def requestWithoutLiebling(session: UserSession, userUrns: Set[Urn], responseJson: JsValue, response: Response): Future[ResponseBuilder] = {
    followCountsClient.counts(session, userUrns.toSeq).map {
      followCountsResponse => {
        val followCountsMap = followCountsResponse.map(count => (count.userUrn, count)).toMap

        val content: String = injectKeys(responseJson, (id) => {
          val userUrn = new Urn("soundcloud", "users", id.toString)
          val followCounts = followCountsMap.get(userUrn).getOrElse(FollowCounts(userUrn, 0, 0))

          List(
            "followers_count" -> followCounts.followers,
            "followings_count" -> followCounts.followings
          )
        }).toString

        response.setContentString(content)
      }

      toResponseBuilder(response)
    }
  }

  private def requestWithLiebling(session: UserSession, userUrns: Set[Urn], responseJson: JsValue, response: Response): Future[ResponseBuilder] = {
    val followsRequest = followCountsClient.counts(session, userUrns.toSeq)
    val lieblingRequest = lieblingClient.userTotalLikeCount(session, userUrns.toSeq)

    Future.join(followsRequest, lieblingRequest).map {
      case (followCountsResponse, lieblingCountsResponse) => {
        val followCountsMap = followCountsResponse.map(count => (count.userUrn, count)).toMap
        val lieblingCountsMap = lieblingCountsResponse.map(count => (count.user_urn, count)).toMap

        val content: String = injectKeys(responseJson, (id) => {
          val userUrn = new Urn("soundcloud", "users", id.toString)
          val followCounts = followCountsMap.get(userUrn).getOrElse(FollowCounts(userUrn, 0, 0))
          val lieblingCounts = lieblingCountsMap.get(userUrn).getOrElse(UserTotalLikes(userUrn, 0, 0))

          List(
            "followers_count" -> followCounts.followers,
            "followings_count" -> followCounts.followings,
            "public_favorites_count" -> lieblingCounts.totalLikeCount
          )
        }).toString

        response.setContentString(content)
      }

      toResponseBuilder(response)
    }
  }

  private def injectKeys(json: JsValue, fn: Int => Seq[(String, JsValueWrapper)]): JsValue = {
    json \ "collection" match {
      case collection: JsArray =>
        json.as[JsObject] ++ Json.obj("collection" -> injectKeys(collection, fn))

      case _ => json match {
        case JsArray(values) => JsArray(values.map(injectKeysIntoUser(_, fn)))
        case single: JsObject => injectKeysIntoUser(single, fn)
        case _ => json
      }
    }

  }
  private def injectKeysIntoUser(jsValue: JsValue, fn: Int => Seq[(String, JsValueWrapper)]): JsValue = {
    jsValue \ "user" match {
      case user: JsObject =>
        jsValue.as[JsObject] ++ Json.obj("user" -> injectKeysIntoUser(user, fn))

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
