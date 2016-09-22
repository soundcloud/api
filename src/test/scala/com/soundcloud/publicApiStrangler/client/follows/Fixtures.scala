package com.soundcloud.publicApiStrangler.client.follows

import play.api.libs.json._

import scala.io.{Codec, Source}

//object Fixtures {
//  lazy val followEvent = fileJson("follow_event")
//  lazy val unfollowEvent = fileJson("unfollow_event")
//  lazy val userFollowResultPage = fileJson("user_follow_response")
//  lazy val userPurgatoriedEvent = fileJson("user_purgatoried_event")
//  lazy val userRestoredEvent = fileJson("user_restored_event")
//  lazy val userReapedEvent = fileJson("user_reaped_event")
//  lazy val userFixEvent = fileJson("user_fix_event")
//
//  private def fileJson(name: String) =
//    Json.parse(fileToString(name))
//
//  private def fileToString(name: String) =
//    Source.fromURL(getClass.getResource(s"/fixtures/follows/$name.json"))(Codec.UTF8).mkString
//}