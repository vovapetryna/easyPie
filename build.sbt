ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "3.0.0-RC1"
ThisBuild / scalacOptions := Seq("-language:postfixOps")

//--- Dependencies
val circleDeps = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-refined"
).map(_ % "0.14.0-M4")

val refinedDeps = Seq(
  "eu.timepit" %% "refined"
).map(_ % "0.9.23")

val utilDeps = Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3"
) ++ circleDeps ++ refinedDeps

val doobieDeps = Seq(
  "org.tpolecat" %% "doobie-core",
  "org.tpolecat" %% "doobie-postgres",
  "org.tpolecat" %% "doobie-refined"
).map(_ % "0.12.1")

val catsDeps = Seq(
  "org.typelevel" %% "cats-core"   % "2.4.2",
  "org.typelevel" %% "cats-effect" % "2.3.3"
)

val fsDeps = Seq(
  "co.fs2" %% "fs2-core",
  "co.fs2" %% "fs2-io"
).map(_ % "2.5.2")

val serverDeps = Seq(
  "org.http4s" %% "http4s-core",
  "org.http4s" %% "http4s-dsl",
  "org.http4s" %% "http4s-server",
  "org.http4s" %% "http4s-circe",
  "org.http4s" %% "http4s-blaze-server"
).map(_ % "0.22.0-M6")

//--- Projects
lazy val config = project
  .in(file("modules/jsConfig"))
  .settings(libraryDependencies ++= catsDeps ++ fsDeps ++ utilDeps)

lazy val core = project
  .in(file("modules/core"))
  .dependsOn(config)

lazy val database = project
  .in(file("modules/database"))
  .dependsOn(core)
  .settings(libraryDependencies ++= doobieDeps)

lazy val api = project
  .in(file("api"))
  .dependsOn(database, core)
  .settings(libraryDependencies ++= serverDeps)

lazy val root = project
  .in(file("."))
  .aggregate(api)
