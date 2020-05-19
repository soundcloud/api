resolvers := Seq(Resolver.defaultLocal, "SC Repo" at "https://maven.dev.s-cloud.net/sc-repo/")

addSbtPlugin("com.soundcloud" % "sbtkit" % "3.0.2")
addSbtPlugin("com.soundcloud" % "jvmkit-twirp-sbt-plugin" % "12.6.1")
