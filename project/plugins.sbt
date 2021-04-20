resolvers := Seq(Resolver.defaultLocal, "SC Repo" at "https://maven.dev.s-cloud.net/sc-repo/")
scalacOptions += "-Wconf:cat=deprecation:error"

addSbtPlugin("com.soundcloud" % "sbtkit" % "5.1.1")
addSbtPlugin("com.soundcloud" % "jvmkit-twirp-sbt-plugin" % "14.12.0")
