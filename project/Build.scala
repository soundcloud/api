import sbt.Keys._
import sbt._

import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import io.gatling.sbt.GatlingPlugin
import net.virtualvoid.sbt.graph.Plugin.graphSettings
import com.typesafe.sbt.SbtNativePackager.autoImport._

object JvmConfiguration {
  val expectedJavaVersion: String = "1.8"

  def assertUsingExpectedJavaVersion() = {
    val javaVersion = sys.props("java.version")
    val isJava8 = javaVersion.startsWith(expectedJavaVersion)
    require(isJava8, s"Java $expectedJavaVersion is required for this project ($javaVersion found)")
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
        "com.soundcloud"     %% "bff"                 % "14.1.0",
        "com.soundcloud"     %% "follows-client"      % "0.0.2",
        "com.soundcloud"     %% "ratelimitinglib"     % "0.0.5",
        "com.soundcloud"     %% "sc-services"         % "18.0.5",
        "com.fasterxml.uuid" %  "java-uuid-generator" % "3.1.3",
        "commons-codec"      %  "commons-codec"       % "1.9",
        "com.rabbitmq"       %  "amqp-client"         % "3.5.1",
        "org.apache.curator" % "curator-framework"    % "2.7.1",
        "org.apache.curator" % "curator-recipes"      % "2.7.1"
      ).map(_.exclude("org.slf4j", "slf4j-jdk14"))
    )

  lazy val gatlingDependencies = defaultSettings ++ Seq(
    libraryDependencies ++= Seq(
      "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.1.4",
      "io.gatling" % "gatling-test-framework" % "2.1.4")
  )

  lazy val performanceTests = Project(id = "performance",
    base = file("performanceTests"))
    .enablePlugins(GatlingPlugin)
    .configs(GatlingPlugin.Gatling)
    .settings(gatlingDependencies: _*)

  lazy val IntegrationTest = config("it") extend(Test)

  lazy val root = Project(id = "public-api-strangler",
    base = file("."))
    .configs(IntegrationTest)
    .settings(defaultSettings: _*)
    .settings(Defaults.itSettings: _*)
    .settings(mainClass in Compile := Some("com.soundcloud.publicApiStrangler.App"): _*)
    .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
    .enablePlugins(JavaAppPackaging)
}
