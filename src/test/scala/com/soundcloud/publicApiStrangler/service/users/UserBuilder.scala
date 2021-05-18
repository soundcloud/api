package com.soundcloud.publicApiStrangler.service.users

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{
  CreatorSubscription,
  UserRepresentation
}

class UserBuilder {
  private var urn: Urn = Urn("soundcloud", "users", "1")
  private var permalink: String = "giraffe"
  private var username: String = "Dr. G. Raffe"
  private var avatar_url: String = "http://example.com/giraffe.jpg?123456789"
  private var permalink_url: String = "https://soundcloud.com/denis"
  private var city: Option[String] = None
  private var country: Option[String] = None
  private var tracks_count: Int = 1
  private var public_tracks_count: Option[Int] = Some(1)
  private var followers_count: Option[Long] = Some(20000)
  private var followings_count: Option[Long] = Some(20)
  private var verified: Boolean = true
  private var description: Option[String] = Some("I am a nice person")
  private var updated_at: Option[String] = Some("2016/10/10 11:21:36 +0000")
  private var created_at: Option[String] = Some("2014/10/10 11:21:36 +0000")
  private var discogs_name: Option[String] = Some("discogs")
  private var first_name: Option[String] = Some("G")
  private var last_name: Option[String] = Some("Raffe")
  private var full_name: Option[String] = Some("G Raffe")
  private var myspace_name: Option[String] = Some("myspace")
  private var website_title: Option[String] = Some("website title")
  private var website: Option[String] = Some("website")
  private var plan: Option[String] = Some("free")
  private var subscriptions: Seq[CreatorSubscription] = Seq.empty
  private var public_favorites_count: Option[Long] = Some(12L)
  private var public_playlists_count: Option[Int] = Some(55)
  private var comments_count: Option[Int] = Some(11)
  private var likes_count: Option[Long] = Some(500L)
  private var reposts_count: Option[Long] = Some(12L)
  private var online: Boolean = false

  def setUrn(value: Urn) = { urn = value; this }
  def setPermalink(value: String) = { permalink = value; this }
  def setUsername(value: String) = { username = value; this }
  def setAvatarUrl(value: String) = { avatar_url = value; this }
  def setPermalinkUrl(value: String) = { permalink_url = value; this }
  def setCity(value: Option[String]) = { city = value; this }
  def setCountry(value: Option[String]) = { country = value; this }
  def setTracksCount(value: Int) = { tracks_count = value; this }
  def setPublicTracksCount(value: Option[Int]) = { public_tracks_count = value; this }
  def setFollowersCount(value: Option[Long]) = { followers_count = value; this }
  def setFollowingsCount(value: Option[Long]) = { followings_count = value; this }
  def setVerified(value: Boolean) = { verified = value; this }
  def setDescription(value: Option[String]) = { description = value; this }
  def setCreatedAt(value: Option[String]) = { created_at = value; this }
  def setUpdatedAt(value: Option[String]) = { updated_at = value; this }
  def setDiscogsName(value: Option[String]) = { discogs_name = value; this }
  def setFirstName(value: Option[String]) = { first_name = value; this }
  def setLastName(value: Option[String]) = { last_name = value; this }
  def setFullName(value: Option[String]) = { full_name = value; this }
  def setMyspaceName(value: Option[String]) = { myspace_name = value; this }
  def setWebsiteTitle(value: Option[String]) = { website_title = value; this }
  def setWebsite(value: Option[String]) = { website = value; this }
  def setPlan(value: Option[String]) = { plan = value; this }
  def setSubscriptions(value: Seq[CreatorSubscription]) = { subscriptions = value; this }
  def setPublicFavouritesCount(value: Option[Long]) = { public_favorites_count = value; this }
  def setPublicPlaylistsCount(value: Option[Int]) = { public_playlists_count = value; this }
  def setCommentsCount(value: Option[Int]) = { comments_count = value; this }
  def setLikesCount(value: Option[Long]) = { likes_count = value; this }
  def setRepostsCount(value: Option[Long]) = { reposts_count = value; this }
  def setOnline(value: Boolean) = { online = value; this }

  def build: UserRepresentation = {
    UserRepresentation(
      urn = this.urn,
      permalink = this.permalink,
      username = this.username,
      avatar_url = this.avatar_url,
      permalink_url = this.permalink_url,
      city = this.city,
      country = this.country,
      tracks_count = this.tracks_count,
      public_tracks_count = this.public_tracks_count,
      followers_count = this.followers_count,
      followings_count = this.followings_count,
      verified = this.verified,
      description = this.description,
      created_at = this.created_at,
      updated_at = this.updated_at,
      discogs_name = this.discogs_name,
      first_name = this.first_name,
      last_name = this.last_name,
      full_name = this.full_name,
      myspace_name = this.myspace_name,
      website_title = this.website_title,
      website = this.website,
      plan = this.plan,
      subscriptions = this.subscriptions,
      public_favorites_count = this.public_favorites_count,
      public_playlists_count = this.public_playlists_count,
      comments_count = this.comments_count,
      likes_count = this.likes_count,
      reposts_count = this.reposts_count,
      online = this.online
    )
  }
}

object UserBuilder {
  def user(userId: Long): UserRepresentation = {
    val builder = new UserBuilder()
    builder.setUrn(urnFor(userId))
    builder.build
  }

  def urnFor(userId: Long): Urn = Urn("soundcloud", "users", userId.toString)
}
