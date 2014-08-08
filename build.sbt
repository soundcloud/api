import com.typesafe.sbt.SbtStartScript

seq(SbtStartScript.startScriptForClassesSettings: _*)

net.virtualvoid.sbt.graph.Plugin.graphSettings

name := "public-api-strangler"

scalaVersion := "2.10.3"

organization := "com.soundcloud"

version := "0.1.0-SNAPSHOT"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-target:jvm-1.6", "-language:_")

resolvers ++= Seq(
  "SoundCloud Internal - Hosted Snapshots" at "http://maven.int.s-cloud.net/content/groups/hosted_snapshots/",
  "SoundCloud Internal - Hosted Releases" at "http://maven.int.s-cloud.net/content/groups/hosted_releases/",
  "SoundCloud Internal - Proxy Snapshots" at "http://maven.int.s-cloud.net/content/groups/proxy_snapshots/",
  "SoundCloud Internal - Proxy Releases" at "http://maven.int.s-cloud.net/content/groups/proxy_releases/"
)

libraryDependencies ++= Seq(
  "com.twitter"    %% "finagle-memcached" % "6.18.0" exclude("org.slf4j", "slf4j-jdk14"),
  "com.soundcloud" %% "bff"  			  % "0.4.22" exclude("org.slf4j", "slf4j-jdk14")
)

mainClass in Compile := Some("com.soundcloud.bff.Main")
