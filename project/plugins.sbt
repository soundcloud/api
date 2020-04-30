resolvers := Seq(Resolver.defaultLocal, "SC Repo" at "https://maven.dev.s-cloud.net/sc-repo/")

addSbtPlugin("com.soundcloud" % "sbtkit" % "2.16.0")
addSbtPlugin("com.soundcloud" % "jvmkit-twirp-sbt-plugin" % "12.6.1")
