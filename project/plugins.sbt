resolvers := Seq(Resolver.defaultLocal, "SC Repo" at "https://maven.dev.s-cloud.net/sc-repo/")

addSbtPlugin("com.soundcloud" % "sbtkit" % "4.0.0")
addSbtPlugin("com.soundcloud" % "jvmkit-twirp-sbt-plugin" % "14.11.1")
