val mainSettings = Seq(
  name := "easyPie",
  version := "0.1.0",
  scalaVersion := "3.0.0-RC1",
  scalacOptions := Seq("-language:postfixOps")
)

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

val utilDeps = circleDeps ++ refinedDeps

val doobieDeps = Seq(
  "org.tpolecat" %% "doobie-core",
  "org.tpolecat" %% "doobie-postgres",
  "org.tpolecat" %% "doobie-refined"
).map(_ % "0.12.1")

val catsDeps = Seq(
  "org.typelevel" %% "cats-core"   % "2.4.2",
  "org.typelevel" %% "cats-effect" % "2.3.3"
)

val serverDeps = Seq(
  "org.http4s" %% "http4s-core",
  "org.http4s" %% "http4s-dsl",
  "org.http4s" %% "http4s-server",
  "org.http4s" %% "http4s-circe"
).map(_ % "0.22.0-M6")

//--- Projects

lazy val core = project
  .in(file("modules/core"))
  .settings(mainSettings: _*)
  .settings(libraryDependencies ++= catsDeps ++ utilDeps)

lazy val database = project
  .in(file("modules/database"))
  .settings(mainSettings: _*)
  .settings(libraryDependencies ++= catsDeps ++ utilDeps)
  .settings(libraryDependencies ++= doobieDeps)
  .dependsOn(core)

lazy val api = project
  .in(file("api"))
  .settings(mainSettings: _*)
  .dependsOn(database)
  .settings(libraryDependencies ++= catsDeps ++ utilDeps)
  .settings(libraryDependencies ++= doobieDeps)
  .settings(libraryDependencies ++= serverDeps)

lazy val root = project
  .in(file("."))
  .settings(mainSettings: _*)
  .aggregate(api)
