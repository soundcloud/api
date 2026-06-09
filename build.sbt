val jvmkitVersion = "17.8.2"
val specs2Version = "4.12.0"
val httpComponentsVersion = "4.5.14"

lazy val apiPublic = project
  .in(file("."))
  .enablePlugins(SbtKitPlugin, TwirpSbtPlugin)
  .settings(
    name := "api-public",
    scalaVersion := "2.12.21",
    scalacOptions += "-Wconf:cat=deprecation:is,any:e",
    libraryDependencies ++= Seq(
      "com.soundcloud" %% "jvmkit-http-client" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-admin-server" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-bff" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-rollout" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-json-play" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-outcome" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-twirp" % jvmkitVersion,
      "com.softwaremill.diffx" %% "diffx-core" % "0.4.5",
      "org.jsoup" % "jsoup" % "1.18.3",
      "io.lemonlabs" %% "scala-uri" % "1.5.1",
      "com.auth0"         % "java-jwt"             % "3.18.2",
      "com.google.auth"   % "google-auth-library-oauth2-http" % "1.32.1",
      "com.squareup.okhttp3" % "mockwebserver" % "3.11.0" % "test",
      "org.apache.httpcomponents" % "httpclient" % httpComponentsVersion % "test",
      "org.apache.httpcomponents" % "httpmime" % httpComponentsVersion % "test",
      "org.specs2" %% "specs2-core" % specs2Version % "test",
      "org.specs2" %% "specs2-mock" % specs2Version % "test"
    ),
    Compile / mainClass := Some("com.soundcloud.apipublic.App")
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
    scalaVersion := "2.12.21",
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
