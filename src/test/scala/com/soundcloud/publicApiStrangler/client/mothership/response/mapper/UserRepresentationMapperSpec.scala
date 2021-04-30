package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures

class UserRepresentationMapperSpec extends UnitSpecification {
  trait Context extends Scope {
    lazy val userJson = Fixtures.moshiUser

    lazy val user = UserRepresentationMapper(userJson)
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

  "maps the updated at datetime" in new Context {
    user.updated_at ==== Some("2014/05/16 02:43:00 +0000")
  }
}
