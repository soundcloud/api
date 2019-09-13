val SC = "https://maven.dev.s-cloud.net/sc-repo/"
externalResolvers := Seq(Resolver.defaultLocal, "SC Repo" at SC)
sbtResolvers := Seq(Resolver.url("SC", url(SC))(Resolver.ivyStylePatterns))

addSbtPlugin("com.soundcloud" % "sbtkit" % "2.10.0")
