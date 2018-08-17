name := """bitcoin-prediction-service"""
organization := "com.tookitaki"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  guice,
  ws,
  ehcache,
  "org.apache.spark" %% "spark-core" % "2.3.1",
  "org.apache.spark" %% "spark-mllib" % "2.3.1",
  "joda-time" % "joda-time" % "2.8.1",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.8.8"
)
libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test)

routesGenerator := InjectedRoutesGenerator

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.tookitaki.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.tookitaki.binders._"
