package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCounts
import com.soundcloud.publicApiStrangler.client.liebling.UserTotalLikes
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{CreatorSubscription, Product}
import com.soundcloud.publicApiStrangler.subscriptions.{Package, SubmarineCreatorSubscription}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures

class UserRepresentationMapperSpec extends UnitSpecification {
  trait Context extends Scope {

    val userUrn = Urn("soundcloud", "users", "10419549")
    val `package` = mock[Package]
    val submarineSubscription = mock[SubmarineCreatorSubscription]
    submarineSubscription.`package` returns `package`
    val subscriptions = Map(userUrn -> Some(submarineSubscription))

    val followCounts = Some(Map(userUrn -> FollowCounts(userUrn, 123, 456)))
    val repostCounts = Some(Map(userUrn -> 789L))
    val totalLikesCount = Some(Map(userUrn -> UserTotalLikes(userUrn, 2, 4)))

    lazy val userJson = Fixtures.moshiUser
    lazy val userMapper = UserRepresentationMapper(userJson)
  }

  "maps the urn" in new Context {
    userMapper.urn ==== Urn("soundcloud", "users", "10419549")
  }

  "maps the permalink" in new Context {
    userMapper.permalink ==== "eric"
  }

  "maps the username" in new Context {
    userMapper.username ==== "Eric"
  }

  "maps the avatar url" in new Context {
    userMapper.avatar_url ==== "http://i1.sndcdn.com/avatars-000006111783-xqaxy3-large.jpg?16b9957"
  }

  "maps the permalink url" in new Context {
    userMapper.permalink_url ==== "http://soundcloud.com/eric"
  }

  "maps the city" in new Context {
    userMapper.city ==== Some("Berlin")
  }

  "maps the country" in new Context {
    userMapper.country ==== Some("Germany")
  }

  "maps the tracks count" in new Context {
    userMapper.tracks_count ==== 349
  }

  "maps the followers count" in new Context {
    userMapper.followers_count ==== Some(22737)
  }

  "maps the followings count" in new Context {
    userMapper.followings_count ==== Some(1430)
  }

  "maps the description" in new Context {
    userMapper.description ==== Some("Founder/CTO SoundCloud.\r\nMusician under the alias http://soundcloud.com/forss")
  }

  "maps the updated at datetime" in new Context {
    userMapper.updated_at ==== Some("2014/05/16 02:43:00 +0000")
  }

  "maps a 'pro' plan to legacy plan value" in new Context {
    `package`.plan returns "pro"
    override lazy val userMapper = UserRepresentationMapper(json = userJson, maybeSubscriptions = Some(subscriptions))
    userMapper.plan ==== Some("Pro")
  }

  "maps a 'pro-unlimited' plan to legacy plan value" in new Context {
    `package`.plan returns "pro-unlimited"
    override lazy val userMapper = UserRepresentationMapper(json = userJson, maybeSubscriptions = Some(subscriptions))
    userMapper.plan ==== Some("Pro Plus")
  }

  "maps plan to submarine value for unhandled submarine plans" in new Context {
    `package`.plan returns "unheard-of-plan"
    override lazy val userMapper = UserRepresentationMapper(json = userJson, maybeSubscriptions = Some(subscriptions))
    userMapper.plan ==== Some("unheard-of-plan")
  }

  "maps plan to moshimohsi value when there is no submarine plans" in new Context {
    `package`.plan returns "unheard-of-plan"
    override lazy val userMapper = UserRepresentationMapper(json = userJson, maybeSubscriptions = None)
    userMapper.plan ==== Some("Pro Unlimited")
  }

  "maps subscriptions to moshimoshi value if there's no creator subscriptions" in new Context {
    val expectedSubscription = CreatorSubscription(Product("creator-pro-unlimited", "Pro Unlimited"), None)
    userMapper.subscriptions ==== Seq(expectedSubscription)
  }

  "maps follows counts" in new Context {
    override lazy val userMapper = UserRepresentationMapper(json = userJson, maybeFollowCounts = followCounts)
    userMapper.followers_count ==== Some(123)
    userMapper.followings_count ==== Some(456)
  }

  "maps reposts counts" in new Context {
    override lazy val userMapper = UserRepresentationMapper(json = userJson, maybeRepostsCounts = repostCounts)
    userMapper.reposts_count ==== Some(789)
  }

  "maps likes counts" in new Context {
    override lazy val userMapper = UserRepresentationMapper(json = userJson, maybeTotalLikesCounts = totalLikesCount)
    userMapper.public_favorites_count ==== Some(6)
  }

  "adds fields when the user being mapped is the logged-in user" in new Context {
    override lazy val userMapper = UserRepresentationMapper(json = userJson, currentUser = Some(userUrn))
    userMapper.private_tracks_count ==== Some(5L)
    userMapper.private_playlists_count ==== Some(0L)
    userMapper.primary_email_confirmed ==== Some(true)
    userMapper.locale ==== Some("en_GB")
  }
}
