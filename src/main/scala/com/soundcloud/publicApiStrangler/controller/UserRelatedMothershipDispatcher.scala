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

/**
  * So this class is weird. It grew out of this abstraction:
  * https://github.com/soundcloud/public-api-strangler/blob/884e0/src/main/scala/com/soundcloud/publicApiStrangler/controller/FollowCountsHelper.scala
  * which was created to load follower count information into the many ways the public-api returns user representations.
  * currently it also load like counts from liebling.
  *
  * To understand what it does, first look for usages of the `dispatchToMothership` method below.
  * It essentially "plugs in" to any json response coming from the mothership and looks for things that look like
  * User representations. If it finds one, it tries to load the Follow/Like counts and injects that data into the
  * representation with which it was provided.
  *
  * For instance, some mothership endpoints return users that look like:
  * { id: 1, name: Filipe }
  * but sometimes:
  * [ { id: 1, name: Filipe }, { id: 2, name: Argha } ]
  * and sometimes:
  * [ { kind: friend, connection_ids: [...], user: { ... } ]
  * and some other formats as well.
  *
  * And this abstraction tries to look for the right place to insert the `followers_count`, `followings_count`,
  * and `public_favorites_count` keys, with the counts themselves coming from the upstream services that serve them.
  */
class UserRelatedMothershipDispatcher(userAuthentication: UserAuthentication,
                                      mothershipDispatcher: DispatchToMothershipHandler,
                                      followCountsClient: FollowCountsClient,
                                      lieblingClient: LieblingClient,
                                      shouldLoadCountsFromLiebling: () => Future[Boolean]) {

  def dispatchToMothership(request: Request): Future[ResponseBuilder] = {
    userAuthentication.withUserSession(request) { session =>
      mothershipDispatcher.defaultHandling(new HandlerRequest(AlwaysMatchesPathMatcher, request)).flatMap(response => {
        lazy val defaultResponse = toResponseBuilder(response).toFuture

        if (response.getStatusCode() < 300) {
          (for {
            responseJson <- Try(Json.parse(response.getContentString())).toOption
            userUrns = extractUserUrns(responseJson)
            if userUrns.nonEmpty
          } yield {
            enrichResponse(session, userUrns, responseJson, response).map(toResponseBuilder)
          }).getOrElse(defaultResponse)
        } else {
          defaultResponse
        }
      })
    }
  }

  private def enrichResponse(session: UserSession, userUrns: Set[Urn], responseJson: JsValue, response: Response): Future[Response] =
    (
      for {
        followsSubs <- followsSubstitutions(session, userUrns)
        likesSubs <- lieblingSubstitutions(session, userUrns)
      } yield {
        userUrns.map { urn =>
          (urn, followsSubs.getOrElse(urn, List.empty) ++ likesSubs.getOrElse(urn, List.empty))
        }.toMap
      }
    ).map { allSubs: Map[Urn,List[(String,JsValueWrapper)]]  =>
      response.setContentString(
        injectKeys(responseJson, id => allSubs.getOrElse(Urn("soundcloud", "users", id.toString), List.empty)).toString
      )
      response
    }

  private def followsSubstitutions(session: UserSession, userUrns: Set[Urn]): Future[Map[Urn,List[(String,JsValueWrapper)]]] =
    followCountsClient.counts(session, userUrns.toSeq)
      .map(_.map(count => (count.userUrn, count)).toMap)
      .map { fetchedData: Map[Urn,FollowCounts] =>
        userUrns
          .map { urn: Urn => fetchedData.getOrElse(urn, FollowCounts(urn, 0, 0)) }
          .map { followCounts => (
            followCounts.userUrn,
            List(
              "followers_count" -> Json.toJsFieldJsValueWrapper(followCounts.followers),
              "followings_count" -> Json.toJsFieldJsValueWrapper(followCounts.followings)
            )
          )}.toMap
      }

  private def lieblingSubstitutions(session: UserSession, userUrns: Set[Urn]): Future[Map[Urn,List[(String,JsValueWrapper)]]] =
    shouldLoadCountsFromLiebling().flatMap {
      case true =>
        lieblingClient.userTotalLikeCount(session, userUrns.toSeq)
          .map(_.map(count => (count.user_urn, count)).toMap)
          .map { fetchedData: Map[Urn,UserTotalLikes] =>
            userUrns
              .map { urn: Urn => fetchedData.getOrElse(urn, UserTotalLikes(urn, 0, 0)) }
              .map { likeCounts => (
                likeCounts.user_urn,
                List("public_favorites_count" -> Json.toJsFieldJsValueWrapper(likeCounts.totalLikeCount))
              )}.toMap
          }
      case false =>
        Future.value(Map.empty)
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
