import com.soundcloud.jvmkit.sbt.{BffApi, JvmkitApp, HttpServerAppBuild}
import sbt._

object Build extends HttpServerAppBuild(
  JvmkitApp(
    appType = BffApi,
    specificJvmKitVersion = "19.2.0",
    specificScalaVersion = "2.11.6"
  ),
  specificLibDependencies = Seq(
      "com.soundcloud"     %% "follows-client"      % "0.0.2",
      "com.soundcloud"     %% "ratelimitinglib"     % "0.2.0",
      "com.soundcloud"     %% "sc-services"         % "27.0.0",
      "com.fasterxml.uuid" %  "java-uuid-generator" % "3.1.3",
      "commons-codec"      %  "commons-codec"       % "1.9"
  ),
  mainClass = "com.soundcloud.publicApiStrangler.App"
)
