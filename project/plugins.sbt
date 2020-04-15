resolvers := Seq(Resolver.defaultLocal, "SC Repo" at "https://maven.dev.s-cloud.net/sc-repo/")

val jvmkitVersion = "12.5.1" // Keep this in sync with build.sbt
val sbtkitVersion = "2.16.0"

addSbtPlugin("com.soundcloud" % "jvmkit-twirp-sbt-plugin" % jvmkitVersion)
addSbtPlugin("com.soundcloud" % "sbtkit" % sbtkitVersion)

