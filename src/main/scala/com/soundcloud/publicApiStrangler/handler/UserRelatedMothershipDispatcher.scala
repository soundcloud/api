package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.liebling.{LieblingClient, UserTotalLikes}
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.handler.UserRelatedMothershipDispatcher.SubstitutionsByUser
import com.twitter.finagle.http.Response
import com.twitter.util.{Future, Try}
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

/**
  * This class is weird. It grew out of this abstraction:
  * https://github.com/soundcloud/public-api-strangler/blob/884e0/src/main/scala/com/soundcloud/publicApiStrangler/controller/FollowCountsHelper.scala
  * which was created to load follower count information into the many ways the public-api returns user representations.
  * It now enriches such users representations with counts from several sources.
  *
  * To understand what it does, first look for usages of the `dispatchToMothership` method below.
  * If there are any users in the Mothership response, it will enrich them with counts from other services.
  *
  * For instance, some mothership endpoints return users that look like:
  * { id: 1, name: Filipe }
  * but sometimes:
  * [ { id: 1, name: Filipe }, { id: 2, name: Argha } ]
  * and sometimes:
  * [ { kind: friend, connection_ids: [...], user: { ... } ]
  * and some other formats as well.
  *
  * And this abstraction tries to look for the right place to insert values for the `followers_count`,
  * `followings_count`, `public_favorites_count` and other keys, with the counts themselves coming from the
  * upstream services that are responsible for them.
  */
class UserRelatedMothershipDispatcher(
    userAuthentication: UserAuthentication,
    mothershipDispatcher: DispatchToMothershipHandler,
    followCountsClient: FollowCountsClient,
    lieblingClient: LieblingClient,
    shouldLoadCountsFromLiebling: () => Future[Boolean],
    repostsClient: RepostsClient
) {
  def dispatchToMothership(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      mothershipDispatcher
        .dispatchToMothership(request.request)
        .flatMap(response => {
          lazy val defaultResponse = Future.value(response)

          if (response.statusCode < 300) {
            (for {
              responseJson <- Try(Json.parse(response.getContentString())).toOption
              userUrns = extractUserUrns(responseJson)
              if userUrns.nonEmpty
            } yield {
              enrichResponse(session, userUrns, responseJson, response)
            }).getOrElse(defaultResponse)
          } else {
            defaultResponse
          }
        })
    }
  }

  private def enrichResponse(
      session: UserSession,
      userUrns: Set[Urn],
      responseJson: JsValue,
      response: Response
  ): Future[Response] =
    Future
      .join(
        followsSubstitutions(session, userUrns),
        lieblingSubstitutions(session, userUrns),
        repostsSubstitutions(session, userUrns)
      )
      .map {
        case (followsSubs, likesSubs, repostsSubs) =>
          userUrns.map { urn =>
            (
              urn,
              followsSubs.getOrElse(urn, List.empty)
                ++ likesSubs.getOrElse(urn, List.empty)
                ++ repostsSubs.getOrElse(urn, List.empty)
            )
          }.toMap
      }
      .map { allSubs: SubstitutionsByUser =>
        response.setContentString(
          injectKeys(responseJson, id => allSubs.getOrElse(Urn("soundcloud", "users", id.toString), List.empty)).toString
        )
        response
      }

  private def followsSubstitutions(session: UserSession, userUrns: Set[Urn]): Future[SubstitutionsByUser] =
    followCountsClient
      .counts(session, userUrns.toSeq)
      .map(_.map(count => (count.userUrn, count)).toMap)
      .map { fetchedData: Map[Urn, FollowCounts] =>
        userUrns
          .map { urn: Urn =>
            fetchedData.getOrElse(urn, FollowCounts(urn, 0, 0))
          }
          .map { followCounts =>
            (
              followCounts.userUrn,
              List(
                "followers_count" -> Json.toJsFieldJsValueWrapper(followCounts.followers),
                "followings_count" -> Json.toJsFieldJsValueWrapper(followCounts.followings)
              )
            )
          }
          .toMap
      }

  private def repostsSubstitutions(session: UserSession, userUrns: Set[Urn]): Future[SubstitutionsByUser] = {
    repostsClient
      .getRepostCountsByUrnWithFallback(session, userUrns)
      .map(_.map { case (urn, count) => (urn, List("reposts_count" -> Json.toJsFieldJsValueWrapper(count))) }.toMap)
  }

  private def lieblingSubstitutions(session: UserSession, userUrns: Set[Urn]): Future[SubstitutionsByUser] =
    shouldLoadCountsFromLiebling().flatMap {
      case true =>
        lieblingClient
          .userTotalLikeCount(session, userUrns.toSeq)
          .map(_.map(count => (count.user_urn, count)).toMap)
          .map { fetchedData: Map[Urn, UserTotalLikes] =>
            userUrns
              .map { urn: Urn =>
                fetchedData.getOrElse(urn, UserTotalLikes(urn, 0, 0))
              }
              .map { likeCounts =>
                (
                  likeCounts.user_urn,
                  List("public_favorites_count" -> Json.toJsFieldJsValueWrapper(likeCounts.totalLikeCount))
                )
              }
              .toMap
          }
      case false =>
        Future.value(Map.empty)
    }

  private def injectKeys(json: JsValue, fn: Int => Seq[(String, JsValueWrapper)]): JsValue = {
    json \ "collection" match {
      case JsDefined(collection: JsArray) =>
        json.as[JsObject] ++ Json.obj("collection" -> injectKeys(collection, fn))

      case _ =>
        json match {
          case JsArray(values) => JsArray(values.map(injectKeysIntoUser(_, fn)))
          case single: JsObject => injectKeysIntoUser(single, fn)
          case _ => json
        }
    }
  }

  private def injectKeysIntoUser(jsValue: JsValue, fn: Int => Seq[(String, JsValueWrapper)]): JsValue = {
    jsValue \ "user" match {
      case JsDefined(user: JsObject) =>
        jsValue.as[JsObject] ++ Json.obj("user" -> injectKeysIntoUser(user, fn))

      case _ =>
        (jsValue \ "kind")
          .asOpt[String]
          .flatMap {
            case "user" =>
              (jsValue \ "id").asOpt[Int].map { id =>
                val newValues = fn(id)
                jsValue.as[JsObject] ++ Json.obj(newValues: _*)
              }
            case _ => None
          }
          .getOrElse(jsValue)
    }
  }

  private def extractUserUrns(json: JsValue): Set[Urn] = {
    json \ "collection" match {
      case JsDefined(collection: JsArray) => extractUserUrns(collection)

      case _ =>
        json match {
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
}

object UserRelatedMothershipDispatcher {
  type SubstitutionsByUser = Map[Urn, List[(String, JsValueWrapper)]]
}
