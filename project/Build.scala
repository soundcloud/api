import com.soundcloud.jvmkit.sbt.{BffApi, JvmkitApp, HttpServerAppBuild}
import sbt._
import sbt.Keys._

object Build extends HttpServerAppBuild(
  JvmkitApp(
    appType = BffApi,
    jvmKitVersion = "23.0.1",
    scalaVersion = "2.11.6"
  ),
  libDependencies = Seq(
    "com.soundcloud"     %% "follows-client"      % "0.0.2",
    "com.soundcloud"     %% "ratelimitinglib"     % "0.2.7",
    "com.soundcloud"     %% "sc-services"         % "27.0.0",
    "com.fasterxml.uuid" %  "java-uuid-generator" % "3.1.3",
    "commons-codec"      %  "commons-codec"       % "1.9"
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
          "com.soundcloud" %% "jvmkit" % "23.0.1"
        )
      )
    )
}
