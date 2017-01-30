import com.soundcloud.jvmkit.sbt.{BffApi, HttpServerAppBuild, JvmkitApp}
import sbt.Keys._
import sbt._

object BuildProperties {
  val jvmkitVersion = "51.2.0"
}

object Build extends HttpServerAppBuild(
  JvmkitApp(
    name = "public-api-strangler",
    appType = BffApi,
    jvmKitVersion = BuildProperties.jvmkitVersion,
    scalaVersion = "2.11.8"
  ),
  libDependencies = Seq(
    "com.soundcloud" %% "sc-services" % "48.0.0",
    "com.soundcloud" %% "timeline-client" % "0.1.2",
    "com.soundcloud" %% "ratelimitinglib" % BuildProperties.jvmkitVersion,
    "com.fasterxml.uuid" % "java-uuid-generator" % "3.1.3",
    "commons-codec" % "commons-codec" % "1.9",
    "org.jsoup" % "jsoup" % "1.8.3",
    "com.squareup.okhttp3" % "mockwebserver" % "3.2.0" % "test",
    "org.apache.httpcomponents" % "httpclient" % "4.5.2" % "test",
    "org.apache.httpcomponents" % "httpmime" % "4.5.2" % "test",
    ("au.com.dius" %% "pact-jvm-consumer-specs2" % "3.3.4")
      .excludeAll(ExclusionRule(organization = "com.fasterxml.jackson.core"))
  ),
  mainClass = "com.soundcloud.publicApiStrangler.App"
) {

  lazy val endToEnd = Project(
    id = "endToEnd",
    base = file("endToEndTests"),
    settings = generalSettings ++
      Seq(libraryDependencies ++= Seq(
        "org.specs2" %% "specs2-core" % "3.6.4",
        "org.specs2" %% "specs2-mock" % "3.6.4",
        "com.soundcloud" %% "jvmkit" % BuildProperties.jvmkitVersion,
        "com.soundcloud" %% "jvmkit-testing" % BuildProperties.jvmkitVersion % "test",
        "org.apache.httpcomponents" % "httpclient" % "4.5.2",
        "org.apache.httpcomponents" % "httpmime" % "4.5.2"
      ))
  )
}
