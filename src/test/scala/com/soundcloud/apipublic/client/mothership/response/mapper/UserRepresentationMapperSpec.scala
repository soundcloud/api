package com.soundcloud.apipublic.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.client.followcounts.FollowCounts
import com.soundcloud.apipublic.client.mothership.response.representation.{CreatorSubscription, Product}
import com.soundcloud.apipublic.service.users.UserUploadQuota
import com.soundcloud.apipublic.subscriptions.{Package, SubmarineCreatorSubscription}
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.apipublic.test.fixtures.Fixtures

class UserRepresentationMapperSpec extends UnitSpecification {
  trait Context extends Scope {

    val userUrn = Urn("soundcloud", "users", "10419549")
    val `package` = mock[Package]
    val submarineSubscription = mock[SubmarineCreatorSubscription]
    submarineSubscription.`package` returns `package`
    val subscriptions = Map(userUrn -> Some(submarineSubscription))

    val followCounts = Some(Map(userUrn -> FollowCounts(userUrn, 123, 456)))
    val repostCounts = Some(Map(userUrn -> 789L))
    val totalLikesCount = Some(Map(userUrn -> 6L))
    val uploadQuota = UserUploadQuota(1, Some(2))

    lazy val userJson = Fixtures.moshiUser
    lazy val user = UserRepresentationMapper(userJson, loggedInUser = Some(userUrn))
  }

  "maps the urn" in new Context {
    user.urn ==== Urn("soundcloud", "users", "10419549")
  }

  "maps the permalink" in new Context {
    user.permalink ==== "eric"
  }

  "maps the username" in new Context {
    user.username ==== "Eric"
  }

  "maps the avatar url" in new Context {
    user.avatar_url ==== "http://i1.sndcdn.com/avatars-000006111783-xqaxy3-large.jpg?16b9957"
  }

  "maps the permalink url" in new Context {
    user.permalink_url ==== "http://soundcloud.com/eric"
  }

  "maps the city" in new Context {
    user.city ==== Some("Berlin")
  }

  "maps the country" in new Context {
    user.country ==== Some("Germany")
  }

  "maps the tracks count" in new Context {
    user.tracks_count ==== 349
  }

  "maps the followers count" in new Context {
    user.followers_count ==== Some(22737)
  }

  "maps the followings count" in new Context {
    user.followings_count ==== Some(1430)
  }

  "maps the description" in new Context {
    user.description ==== Some("Founder/CTO SoundCloud.\r\nMusician under the alias http://soundcloud.com/forss")
  }

  "maps the updated at datetime for the signed-in user" in new Context {
    user.updated_at ==== Some("2014/05/16 02:43:00 +0000")
  }

  "returns mock created_at and updated_at for users that are not the signed-in user" in new Context {
    override lazy val user = UserRepresentationMapper(userJson, loggedInUser = None)
    user.created_at ==== Some(UserRepresentationMapper.MockTimestampForOtherUsers)
    user.updated_at ==== Some(UserRepresentationMapper.MockTimestampForOtherUsers)
  }

  "maps a 'pro' plan to legacy plan value" in new Context {
    `package`.plan returns "pro"
    override lazy val user = UserRepresentationMapper(json = userJson, maybeSubscriptions = Some(subscriptions))
    user.plan ==== Some("Pro")
  }

  "maps a 'pro-unlimited' plan to legacy plan value" in new Context {
    `package`.plan returns "pro-unlimited"
    override lazy val user = UserRepresentationMapper(json = userJson, maybeSubscriptions = Some(subscriptions))
    user.plan ==== Some("Pro Plus")
  }

  "maps plan to submarine value for unhandled submarine plans" in new Context {
    `package`.plan returns "unheard-of-plan"
    override lazy val user = UserRepresentationMapper(json = userJson, maybeSubscriptions = Some(subscriptions))
    user.plan ==== Some("unheard-of-plan")
  }

  "maps plan to moshimohsi value when there is no submarine plans" in new Context {
    `package`.plan returns "unheard-of-plan"
    override lazy val user = UserRepresentationMapper(json = userJson, maybeSubscriptions = None)
    user.plan ==== Some("Pro Unlimited")
  }

  "maps subscriptions to moshimoshi value if there's no creator subscriptions" in new Context {
    val expectedSubscription = CreatorSubscription(Product("creator-pro-unlimited", "Pro Unlimited"), None)
    user.subscriptions ==== Seq(expectedSubscription)
  }

  "maps follows counts" in new Context {
    override lazy val user = UserRepresentationMapper(json = userJson, maybeFollowCounts = followCounts)
    user.followers_count ==== Some(123)
    user.followings_count ==== Some(456)
  }

  "maps reposts counts" in new Context {
    override lazy val user = UserRepresentationMapper(json = userJson, maybeRepostsCounts = repostCounts)
    user.reposts_count ==== Some(789)
  }

  "maps likes counts" in new Context {
    override lazy val user = UserRepresentationMapper(json = userJson, maybeTotalLikesCounts = totalLikesCount)
    user.public_favorites_count ==== Some(6)
  }
}
