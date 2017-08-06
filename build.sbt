scalaVersion := "2.12.3"

name := "wikipedia-xml-splitter"
organization := "org.alexeyn"
version := "1.0"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "info.bliki.wiki" % "bliki-core" % "3.1.0",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

enablePlugins(JavaAppPackaging)