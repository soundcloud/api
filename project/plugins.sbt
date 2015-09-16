resolvers += Resolver.url("SoundCloud Internal - Proxy Releases", new URL("http://maven.int.s-cloud.net/content/groups/proxy_releases/"))(Resolver.ivyStylePatterns)

//addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.0.4")
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.4.0")
addSbtPlugin("com.soundcloud" % "sbt-jvmkit" % "0.0.42")
