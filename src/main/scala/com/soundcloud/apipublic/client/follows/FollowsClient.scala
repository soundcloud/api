package com.soundcloud.apipublic.client.follows

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.client.follows.mapper._
import com.soundcloud.apipublic.client.follows.representation.follow._
import com.soundcloud.apipublic.client.follows.representation.unfollow._
import com.soundcloud.apipublic.client.follows.representation.{FilteredUserUrns, FollowingsPage}
import com.soundcloud.apipublic.client.support.FetchClient
import com.soundcloud.apipublic.client.chrono.ChronoResponse
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json._

class FollowsClient(jsonService: JsonClient) extends FetchClient {

  /**
    * Follows the target user.
    * Returns `FollowingCreated` when the target user is followed successfully.
    * Returns `AlreadyFollowing` when the target user is already being followed.
    * Returns one of the `FollowingNotPossible` case objects when is not possible to follow the target user.
    * Returns `UnknownError` when an unknown error happens.
    *
    * @see https://github.com/soundcloud/follows#post-followtarget_urn
    */
  def follow(userSession: UserSession, target: Urn): Future[FollowResponse] =
    jsonService
      .postWithSession(
        userSession,
        Path() / "follow" / target,
        Params.empty,
        Headers.empty,
        None
      )
      .map(response => FollowResponseMapper(response))

  // TODO: Improve this once we start tackling writes for the service.
  def bulkFollow(userSession: UserSession, targets: List[Urn]): Future[List[FollowResponse]] = inBatches(targets, 20) {
    urns =>
      jsonService
        .postWithSession(userSession, Path() / "bulkfollow", Params("urns" -> urns), Headers.empty, None)
        .map(response => BulkFollowResponseMapper(response, targets))
  }

  /**
    * Follows the target user.
    * Returns `UnfollowSuccessful` when the target user is unfollowed successfully.
    * Returns one of the `UnfollowingNotPossible` case objects when is not possible to ufollow the target user.
    * Returns `UnknownError` when an unknown error happens.
    *
    * @see https://github.com/soundcloud/follows#delete-unfollowtarget_urn
    */
  def unfollow(userSession: UserSession, target: Urn): Future[UnfollowResponse] =
    jsonService
      .deleteWithSession(
        userSession,
        Path() / "unfollow" / target,
        Params.empty,
        Headers.empty,
        None
      )
      .map(response => UnfollowResponseMapper(response))

  /**
    * Returns a page of followers of the given user, according to the pagination options.
    * Returns `None` in case of error.
    *
    * @see https://github.com/soundcloud/follows#get-usersuser_urnfollowers
    */
  def followers(
      userSession: UserSession,
      user: Urn,
      cursor: Option[String],
      pageSize: Int = 20
  ): Future[Option[FollowingsPage]] =
    fetchPage(
      userSession,
      Path() / "users" / user / "followers",
      cursor,
      pageSize
    )

  /**
    * Returns a page of followings of the given user in chronological order (by follow date).
    * Returns `None` in case of error.
    *
    */
  def followingsChrono(
      userSession: UserSession,
      user: Urn,
      cursor: Option[String],
      limit: Int = 20,
      direction: String = "asc"
  ): Future[Option[ChronoResponse]] =
    fetchChrono(
      userSession,
      Path() / "users" / user / "followings" / "chrono",
      cursor,
      limit,
      direction
    )

  /**
    * Returns a page of followings of the given user, according to the pagination options.
    * Returns `None` in case of error.
    *
    * @see https://github.com/soundcloud/follows#get-usersuser_urnfollowings
    */
  def followings(
      userSession: UserSession,
      user: Urn,
      cursor: Option[String],
      pageSize: Int = 20
  ): Future[Option[FollowingsPage]] =
    fetchPage(
      userSession,
      Path() / "users" / user / "followings",
      cursor,
      pageSize
    )

  /**
    * Returns the candidate users split in two groups, depending if the given user is following them or not.
    * Returns `None` in case of error.
    *
    * @see https://github.com/soundcloud/follows#get-usersuser_urnfilter_followings
    */
  def filterFollowings(
      userSession: UserSession,
      user: Urn,
      candidateUsers: Seq[Urn]
  ): Future[Option[FilteredUserUrns]] =
    filterUsers(
      userSession,
      Path() / "users" / user / "filter_followings",
      candidateUsers
    )

  /**
    * Returns the candidate users split in two groups, depending if they are followers of the given user or not.
    * Returns `None` in case of error.
    *
    * @see https://github.com/soundcloud/follows#get-usersuser_urnfilter_followers
    */
  def filterFollowers(userSession: UserSession, user: Urn, candidateUsers: Seq[Urn]): Future[Option[FilteredUserUrns]] =
    filterUsers(
      userSession,
      Path() / "users" / user / "filter_followers",
      candidateUsers
    )

  private def fetchPage(
      userSession: UserSession,
      path: Path,
      cursor: Option[String],
      pageSize: Int
  ): Future[Option[FollowingsPage]] =
    jsonService
      .getWithSession(
        userSession,
        path,
        cursor.map(id => Params("last_id" -> id)).getOrElse(Params.empty) ++ Params("page_size" -> pageSize),
        Headers.empty
      )
      .map(SimpleMapper[FollowingsPage])

  private def fetchChrono(
      userSession: UserSession,
      path: Path,
      cursor: Option[String],
      limit: Int,
      direction: String
  ): Future[Option[ChronoResponse]] =
    jsonService
      .getWithSession(
        userSession,
        path,
        Params("limit" -> limit, "direction" -> direction) ++ cursor
          .map(c => Params("cursor" -> c))
          .getOrElse(Params.empty),
        Headers.empty
      )
      .map { response =>
        response.status match {
          case Status.Ok => Some(Json.parse(response.contentString).as[ChronoResponse])
          case _ => None
        }
      }

  private def filterUsers(
      userSession: UserSession,
      path: Path,
      candidateUsers: Seq[Urn]
  ): Future[Option[FilteredUserUrns]] =
    jsonService
      .getWithSession(
        userSession,
        path,
        Params("urns" -> candidateUsers),
        Headers.empty
      )
      .map(SimpleMapper[FilteredUserUrns])
}
