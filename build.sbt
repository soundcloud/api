val jvmkitVersion = "14.6.0"
val specs2Version = "4.9.2"
val httpComponentsVersion = "4.5.12"

lazy val publicApiStrangler = project
  .in(file("."))
  .enablePlugins(SbtKitPlugin, TwirpSbtPlugin)
  .settings(
    name := "public-api-strangler",
    libraryDependencies ++= Seq(
      "com.soundcloud" %% "jvmkit-http-client" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-admin-server" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-bff" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-rollout" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-memcached" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-json-play" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-outcome" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-twirp" % jvmkitVersion,
      "com.netaporter" %% "scala-uri" % "0.4.16",
      "org.jsoup" % "jsoup" % "1.11.3",
      "com.squareup.okhttp3" % "mockwebserver" % "3.11.0" % "test",
      "org.apache.httpcomponents" % "httpclient" % httpComponentsVersion % "test",
      "org.apache.httpcomponents" % "httpmime" % httpComponentsVersion % "test",
      "org.specs2" %% "specs2-core" % specs2Version % "test",
      "org.specs2" %% "specs2-mock" % specs2Version % "test"
    ),
    mainClass in Compile := Some("com.soundcloud.publicApiStrangler.App")
  )

lazy val endToEnd = project
  .in(file("endToEndTests"))
  .enablePlugins(SbtKitPlugin)
  .settings(
    name := "endToEnd",
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
