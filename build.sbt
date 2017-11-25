name := "HackerDetectorService"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "org.redisson" % "redisson" % "3.1.0"
)