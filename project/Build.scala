import sbt.Keys._
import sbt._

import com.typesafe.sbt.SbtStartScript
import io.gatling.sbt.GatlingPlugin
import net.virtualvoid.sbt.graph.Plugin.graphSettings


object JvmConfiguration {
  val expectedJavaVersion: String = "1.8"

  def assertUsingExpectedJavaVersion() = {
    val javaVersion = sys.props("java.version")
    val isJava7 = javaVersion.startsWith(expectedJavaVersion)
    require(isJava7, s"Java $expectedJavaVersion is required for this project ($javaVersion found)")
  }

  val javacOptions = Seq("-Xlint:unchecked", "-source", expectedJavaVersion, "-target", expectedJavaVersion)

  val scalaVersion = "2.11.6"
  val scalacoptions = Seq("-deprecation", "-unchecked", s"-target:jvm-${expectedJavaVersion}", "-Xlint")
}

object PublicApiStranglerBuild extends Build {

  JvmConfiguration.assertUsingExpectedJavaVersion

  lazy val defaultSettings = Defaults.coreDefaultSettings ++
    graphSettings ++
    Seq(
      organization := "com.soundcloud",
      name := "public-api-strangler",
      scalaVersion := JvmConfiguration.scalaVersion,
      javacOptions ++= JvmConfiguration.javacOptions,
      scalacOptions ++= JvmConfiguration.scalacoptions,
      incOptions := incOptions.value.withNameHashing(true),
      resolvers ++= Seq(
        "SoundCloud Internal - Hosted Snapshots" at "http://maven.int.s-cloud.net/content/groups/hosted_snapshots/",
        "SoundCloud Internal - Hosted Releases" at "http://maven.int.s-cloud.net/content/groups/hosted_releases/",
        "SoundCloud Internal - Proxy Snapshots" at "http://maven.int.s-cloud.net/content/groups/proxy_snapshots/",
        "SoundCloud Internal - Proxy Releases" at "http://maven.int.s-cloud.net/content/groups/proxy_releases/"
      ),
      externalResolvers := Resolver.withDefaultResolvers(resolvers.value, mavenCentral = false),
      libraryDependencies ++= Seq(
        "com.soundcloud"     %% "bff"                 % "11.2.1-RESPONSE-LIKE-SNAPSHOT",
        "com.soundcloud"     %% "sc-services"         % "16.0.1",
        "com.fasterxml.uuid" %  "java-uuid-generator" % "3.1.3",
        "commons-codec"      %  "commons-codec"       % "1.9",
        "org.apache.curator" % "curator-framework"    % "2.7.1",
        "org.apache.curator" % "curator-recipes"      % "2.7.1"
      ).map(_.exclude("org.slf4j", "slf4j-jdk14"))
    )

  lazy val gatlingDependencies = defaultSettings ++ Seq(
    libraryDependencies ++= Seq(
      "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.1.4" % "it,test",
      "io.gatling" % "gatling-test-framework" % "2.1.4" % "it,test")
  )

  lazy val performanceTests = Project(id = "performance",
    base = file("performanceTests"))
    .enablePlugins(GatlingPlugin)
    .configs(GatlingPlugin.Gatling)
    .settings(gatlingDependencies: _*)

  lazy val root = Project(id = "public-api-strangler",
    base = file("."))
    .configs(IntegrationTest)
    .settings(defaultSettings: _*)
    .settings(Defaults.itSettings: _*)
    .settings(mainClass in Compile := Some("com.soundcloud.publicApiStrangler.App"): _*)
    .settings(SbtStartScript.startScriptForClassesSettings: _*)
    .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
}
