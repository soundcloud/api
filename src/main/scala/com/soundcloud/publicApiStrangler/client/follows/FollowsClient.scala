package com.soundcloud.publicApiStrangler.client.follows

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.follows.mapper._
import com.soundcloud.publicApiStrangler.client.follows.representation.follow._
import com.soundcloud.publicApiStrangler.client.follows.representation.unfollow._
import com.soundcloud.publicApiStrangler.client.follows.representation.{FilteredUserUrns, FollowingsPage, UserUrns}
import com.soundcloud.publicApiStrangler.client.support.FetchClient
import com.twitter.util.Future

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
    jsonService.postWithSession(
      userSession,
      Path() / "follow" / target,
      Params.empty,
      Headers.empty,
      None
    ).map(response => FollowResponseMapper(response))

  // TODO: Improve this once we start tackling writes for the service.
  def bulkFollow(userSession: UserSession, targets: List[Urn]): Future[List[FollowResponse]] = inBatches(targets, 20) { urns =>
    jsonService.postWithSession(
      userSession,
      Path() / "bulkfollow",
      Params("urns" -> urns),
      Headers.empty,
      None).map(response => BulkFollowResponseMapper(response, targets))
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
    jsonService.deleteWithSession(
      userSession,
      Path() / "unfollow" / target,
      Params.empty,
      Headers.empty,
      None
    ).map(response => UnfollowResponseMapper(response))

  /**
    * Returns a list of followers of one user followed by another user.
    * Returns `None` in case of error.
    */
  def followersFollowedBy(userSession: UserSession, user: Urn, anotherUser: Urn): Future[Option[UserUrns]] =
    fetchUrns(
      userSession,
      Path() / "users" / user / "followers_followed" / anotherUser
    )

  /**
    * Returns a list of followings of one user followed by another user.
    * Returns `None` in case of error.
    */
  def followingsNotFollowedBy(userSession: UserSession, user: Urn, anotherUser: Urn): Future[Option[UserUrns]] =
    fetchUrns(
      userSession,
      Path() / "users" / user / "followings_not_followed" / anotherUser
    )

  /**
    * Returns a list of mutual followings between two users.
    * Returns `None` in case of error.
    */
  def mutualFollowings(userSession: UserSession, user: Urn, anotherUser: Urn): Future[Option[UserUrns]] =
    fetchUrns(
      userSession,
      Path() / "users" / user / "mutual_followings" / anotherUser
    )

  /**
    * Returns a page of followers of the given user, according to the pagination options.
    * Returns `None` in case of error.
    *
    * @see https://github.com/soundcloud/follows#get-usersuser_urnfollowers
    */
  def followers(userSession: UserSession,
                user: Urn,
                cursor: Option[String],
                pageSize: Int = 20): Future[Option[FollowingsPage]] =
    fetchPage(
      userSession,
      Path() / "users" / user / "followers",
      cursor,
      pageSize
    )

  /**
    * Returns a page of followings of the given user, according to the pagination options.
    * Returns `None` in case of error.
    *
    * @see https://github.com/soundcloud/follows#get-usersuser_urnfollowings
    */
  def followings(userSession: UserSession,
                 user: Urn,
                 cursor: Option[String],
                 pageSize: Int = 20): Future[Option[FollowingsPage]] =
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
  def filterFollowings(userSession: UserSession, user: Urn, candidateUsers: Seq[Urn]): Future[Option[FilteredUserUrns]] =
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

  private def fetchUrns(userSession: UserSession, path: Path): Future[Option[UserUrns]] =
    jsonService.getWithSession(
      userSession,
      path,
      Params.empty,
      Headers.empty
    ).map(SimpleMapper[UserUrns])

  private def fetchPage(userSession: UserSession,
                        path: Path,
                        cursor: Option[String],
                        pageSize: Int): Future[Option[FollowingsPage]] =
    jsonService.getWithSession(
      userSession,
      path,
      cursor.map(id => Params("last_id" -> id)).getOrElse(Params.empty) ++ Params("page_size" -> pageSize),
      Headers.empty
    ).map(SimpleMapper[FollowingsPage])

  private def filterUsers(userSession: UserSession,
                          path: Path,
                          candidateUsers: Seq[Urn]): Future[Option[FilteredUserUrns]] =
    jsonService.getWithSession(
      userSession,
      path,
      Params("urns" -> candidateUsers),
      Headers.empty
    ).map(SimpleMapper[FilteredUserUrns])
}
