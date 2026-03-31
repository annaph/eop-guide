ThisBuild / organization := "org.eop.guide"

ThisBuild / description := "Effect Oriented Programming Guide"

ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "3.3.6"

ThisBuild / scalacOptions ++= Seq(
  "-java-output-version",
  "21",
  "-encoding",
  "utf8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-explain",
  "-explain-types",
  "-indent",
  "-new-syntax",
  "-print-lines",
  "-Ykind-projector",
  "-Wunused:imports"
)

ThisBuild / fork               := true
ThisBuild / run / connectInput := true

val zioVersion       = "2.1.22"
val zioConfigVersion = "4.0.6"

ThisBuild / libraryDependencies ++= Seq(
  "dev.zio" %% "zio"                 % zioVersion,
  "dev.zio" %% "zio-config"          % zioConfigVersion,
  "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
  "dev.zio" %% "zio-config-magnolia" % zioConfigVersion,
  "dev.zio" %% "zio-test"            % zioVersion % Test,
  "dev.zio" %% "zio-test-sbt"        % zioVersion % Test
)

lazy val root = project
  .in(file("."))
  .settings(name := "eop-guide")
  .aggregate(
    common,
    superpowers,
    initialization,
    testing,
    failure
  )

lazy val common = project.in(file("common"))

lazy val superpowers = project
  .in(file("superpowers"))
  .dependsOn(common)

lazy val initialization = project
  .in(file("initialization"))
  .dependsOn(common)

lazy val testing = project
  .in(file("testing"))
  .dependsOn(common)

lazy val failure = project
  .in(file("failure"))
  .dependsOn(common)
