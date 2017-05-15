resolvers += Resolver.url("SoundCloud Internal - Proxy Releases", new URL("http://maven.int.s-cloud.net/content/groups/proxy_releases/"))(Resolver.ivyStylePatterns)

// These resolvers enable sbt to resolve internally released plugins.
resolvers += new MavenRepository("SoundCloud Internal - Snapshots", "http://maven.int.s-cloud.net/content/repositories/snapshots/")
resolvers += new MavenRepository("SoundCloud Internal - Releases", "http://maven.int.s-cloud.net/content/repositories/releases/")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.4.0")
addSbtPlugin("com.soundcloud" % "sbt-jvmkit" % "0.0.60")
