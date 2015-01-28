import com.typesafe.sbt.SbtStartScript

seq(SbtStartScript.startScriptForClassesSettings: _*)

net.virtualvoid.sbt.graph.Plugin.graphSettings

name := "public-api-strangler"

scalaVersion := "2.10.3"

organization := "com.soundcloud"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-target:jvm-1.7", "-language:_")

resolvers ++= Seq(
  "SoundCloud Internal - Hosted Snapshots" at "http://maven.int.s-cloud.net/content/groups/hosted_snapshots/",
  "SoundCloud Internal - Hosted Releases" at "http://maven.int.s-cloud.net/content/groups/hosted_releases/",
  "SoundCloud Internal - Proxy Snapshots" at "http://maven.int.s-cloud.net/content/groups/proxy_snapshots/",
  "SoundCloud Internal - Proxy Releases" at "http://maven.int.s-cloud.net/content/groups/proxy_releases/"
)

libraryDependencies ++= Seq(
  "com.soundcloud"     %% "bff"                 % "3.3.0",
  "com.soundcloud"     %% "sc-services"         % "5.2.0",
  "com.fasterxml.uuid" %  "java-uuid-generator" % "3.1.3",
  "commons-codec"      %  "commons-codec"       % "1.9"
).map(_.exclude("org.slf4j", "slf4j-jdk14"))

initialize := {
  val javaVersion = sys.props("java.version")
  val isJava8 = javaVersion.startsWith("1.8")
  require(isJava8, "Java 8 is required for this project")
}

mainClass in Compile := Some("com.soundcloud.publicApiStrangler.App")
