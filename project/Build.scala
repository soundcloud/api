import com.soundcloud.jvmkit.sbt.{BffApi, JvmkitApp, HttpServerAppBuild}
import sbt._
import sbt.Keys._

object BuildProperties {
  val jvmkitVersion = "41.1.1-SNAPSHOT"
}

object Build extends HttpServerAppBuild(
  JvmkitApp(
    name = "public-api-strangler",
    appType = BffApi,
    jvmKitVersion = BuildProperties.jvmkitVersion,
    scalaVersion = "2.11.6"
  ),
  libDependencies = Seq(
    "com.soundcloud"     %% "follows-client"           % "2.0.0",
    "com.soundcloud"     %% "timeline-client"          % "0.1.2",
    "com.soundcloud"     %% "ratelimitinglib"          % BuildProperties.jvmkitVersion,
    "com.soundcloud"     %% "sc-services"              % "39.0.0",
    "com.soundcloud"     %% "track-coordinator-client" % "19.0.0",
    "com.fasterxml.uuid" %  "java-uuid-generator"      % "3.1.3",
    "commons-codec"      %  "commons-codec"            % "1.9",
    "com.squareup.okhttp3"      % "mockwebserver" % "3.2.0" % "test",
    "org.apache.httpcomponents" % "httpclient"    % "4.5.2" % "test",
    "org.apache.httpcomponents" % "httpmime"      % "4.5.2" % "test"
  ),
  mainClass = "com.soundcloud.publicApiStrangler.App"
) {

  lazy val endToEnd = Project(
    id = "endToEnd",
    base = file("endToEndTests"),
    settings = generalSettings ++
      Seq(
        libraryDependencies ++= Seq(
          "org.specs2" %% "specs2-core" % "3.6.4",
          "org.specs2" %% "specs2-mock" % "3.6.4",
          "com.soundcloud" %% "jvmkit" % BuildProperties.jvmkitVersion,
          "org.apache.httpcomponents" % "httpclient" % "4.5.2",
          "org.apache.httpcomponents" % "httpmime" % "4.5.2"
        )
      )
    )
}
