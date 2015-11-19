import com.soundcloud.jvmkit.sbt.{BffApi, JvmkitApp, HttpServerAppBuild}
import sbt._
import sbt.Keys._

object Build extends HttpServerAppBuild(
  JvmkitApp(
    appType = BffApi,
    jvmKitVersion = "23.3.0",
    scalaVersion = "2.11.6"
  ),
  libDependencies = Seq(
    "com.soundcloud"     %% "follows-client"           % "0.0.5",
    "com.soundcloud"     %% "ratelimitinglib"          % "0.2.8",
    "com.soundcloud"     %% "sc-services"              % "29.0.0",
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
          "com.soundcloud" %% "jvmkit" % "23.3.0"
        )
      )
    )
}
