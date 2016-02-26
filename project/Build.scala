import com.soundcloud.jvmkit.sbt.{BffApi, JvmkitApp, HttpServerAppBuild}
import sbt._
import sbt.Keys._

object BuildProperties {
  val jvmkitVersion = "31.0.0"
}

object Build extends HttpServerAppBuild(
  JvmkitApp(
    appType = BffApi,
    jvmKitVersion = BuildProperties.jvmkitVersion,
    scalaVersion = "2.11.6"
  ),
  libDependencies = Seq(
    "com.soundcloud"     %% "follows-client"           % "2.0.0",
    "com.soundcloud"     %% "timeline-client"          % "0.1.2",
    "com.soundcloud"     %% "ratelimitinglib"          % "0.4.1",
    "com.soundcloud"     %% "sc-services"              % "35.0.1",
    "com.soundcloud"     %% "track-coordinator-client" % "19.0.0",
    "com.fasterxml.uuid" %  "java-uuid-generator"      % "3.1.3",
    "commons-codec"      %  "commons-codec"            % "1.9"
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
          "com.soundcloud" %% "jvmkit" % BuildProperties.jvmkitVersion
        )
      )
    )
}
