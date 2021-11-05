val jvmkitVersion = "16.3.2"
val specs2Version = "4.12.0"
val httpComponentsVersion = "4.5.12"

lazy val apiPublic = project
  .in(file("."))
  .enablePlugins(SbtKitPlugin, TwirpSbtPlugin)
  .settings(
    name := "api-public",
    scalaVersion := scalaVersion212,
    libraryDependencies ++= Seq(
      "com.soundcloud" %% "jvmkit-http-client" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-admin-server" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-bff" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-rollout" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-memcached" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-json-play" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-outcome" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-twirp" % jvmkitVersion,
      "com.softwaremill.diffx" %% "diffx-core" % "0.4.5",
      "org.jsoup" % "jsoup" % "1.11.3",
      "io.lemonlabs" %% "scala-uri" % "1.5.1",
      "com.squareup.okhttp3" % "mockwebserver" % "3.11.0" % "test",
      "org.apache.httpcomponents" % "httpclient" % httpComponentsVersion % "test",
      "org.apache.httpcomponents" % "httpmime" % httpComponentsVersion % "test",
      "org.specs2" %% "specs2-core" % specs2Version % "test",
      "org.specs2" %% "specs2-mock" % specs2Version % "test"
    ),
    Compile / mainClass := Some("com.soundcloud.publicApiStrangler.App")
  )

// FIXME: upgrade scala-uri lib to fix these simulacrum conflicts, then remove the merge strategy
assembly / assemblyMergeStrategy := {
  case PathList("simulacrum", _*) => MergeStrategy.first
  case x => (assembly / assemblyMergeStrategy).value(x)
}

lazy val endToEnd = project
  .in(file("endToEndTests"))
  .enablePlugins(SbtKitPlugin)
  .settings(
    name := "endToEnd",
    scalaVersion := scalaVersion212,
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % specs2Version,
      "org.specs2" %% "specs2-mock" % specs2Version,
      "org.apache.httpcomponents" % "httpclient" % httpComponentsVersion,
      "org.apache.httpcomponents" % "httpmime" % httpComponentsVersion,
      "com.soundcloud" %% "jvmkit-bff" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-http-client" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-json-play" % jvmkitVersion
    )
  )
