import com.soundcloud.sbtkit.SbtKitPlugin
import sbt.Keys._
import sbt._

val jvmkitVersion = "2.1.0"
val playJsonVersion = "2.5.14"

lazy val publicApiStrangler = project.in(file("."))
  .settings(
    name := "public-api-strangler",
    libraryDependencies ++= Seq(
      "com.netaporter" %% "scala-uri" % "0.4.4",
      "com.soundcloud" %% "jvmkit-http-client" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-admin-server" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-bff" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-rollout" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-memcached" % jvmkitVersion,
      "com.typesafe.play" %% "play-json" % playJsonVersion,
      "com.fasterxml.uuid" % "java-uuid-generator" % "3.1.3",
      "commons-codec" % "commons-codec" % "1.9",
      "org.jsoup" % "jsoup" % "1.8.3",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.squareup.okhttp3" % "mockwebserver" % "3.8.1" % "test",
      "org.apache.httpcomponents" % "httpclient" % "4.5.2" % "test",
      "org.apache.httpcomponents" % "httpmime" % "4.5.2" % "test",
      "org.specs2" %% "specs2-core" % "3.6.4" % "test",
      "org.specs2" %% "specs2-mock" % "3.6.4" % "test",
      ("au.com.dius" %% "pact-jvm-consumer-specs2" % "3.3.4")
        .excludeAll(ExclusionRule(organization = "com.fasterxml.jackson.core"))
    ),
    mainClass in Compile := Some("com.soundcloud.publicApiStrangler.App")
  )
  .enablePlugins(SbtKitPlugin)


lazy val endToEnd = project.in(file("endToEndTests"))
  .settings(
    name := "endToEnd",
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core" % "3.6.4",
      "org.specs2" %% "specs2-mock" % "3.6.4",
      "org.apache.httpcomponents" % "httpclient" % "4.5.2",
      "org.apache.httpcomponents" % "httpmime" % "4.5.2",
      "com.typesafe.play" %% "play-json" % playJsonVersion,
      "com.soundcloud" %% "jvmkit-bff" % jvmkitVersion,
      "com.soundcloud" %% "jvmkit-http-client" % jvmkitVersion
    )
  )
  .enablePlugins(SbtKitPlugin)


