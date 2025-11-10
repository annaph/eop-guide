ThisBuild / organization := "org.eop.guide"

ThisBuild / description := "Effect Oriented Programming Guide"

ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "3.3.6"

ThisBuild / scalacOptions ++= Seq(
  "-java-output-version", "17",
  "-encoding", "utf8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-explain",
  "-explain-types",
  "-indent",
  "-new-syntax",
  "-print-lines",
  "-Ykind-projector"
)

ThisBuild / fork := true
ThisBuild / run / connectInput := true

val zioVersion = "2.1.22"

ThisBuild / libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % zioVersion
)

lazy val root = project
  .in(file("."))
  .settings(name := "eop-guide")
