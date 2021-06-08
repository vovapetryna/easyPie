ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "2.13.5"
ThisBuild / scalacOptions := Seq(
  "-language:postfixOps",
  "-Ymacro-annotations"
)

//--- Dependencies
val refinedV    = "0.9.23"
val refinedDeps = Seq("eu.timepit" %% "refined-pureconfig").map(_ % refinedV)

val utilDeps = Seq(
  "ch.qos.logback"         % "logback-classic" % "1.2.3",
  "com.github.pureconfig" %% "pureconfig"      % "0.14.1"
)

val fsDeps = Seq(
  "co.fs2" %% "fs2-core",
  "co.fs2" %% "fs2-io"
).map(_ % "3.0.1")

val databaseDeps = Seq(
  "io.github.kirill5k" %% "mongo4cats-core",
  "io.github.kirill5k" %% "mongo4cats-circe"
).map(_ % "0.2.4")

val serverDeps = Seq(
  "org.http4s" %% "http4s-core",
  "org.http4s" %% "http4s-dsl",
  "org.http4s" %% "http4s-server",
  "org.http4s" %% "http4s-circe",
  "org.http4s" %% "http4s-blaze-server"
).map(_ % "1.0.0-M21")

val secureDeps = Seq(
  "io.github.jmcardon" %% "tsec-common",
  "io.github.jmcardon" %% "tsec-password",
  "io.github.jmcardon" %% "tsec-jwt-sig",
  "io.github.jmcardon" %% "tsec-jwt-mac"
).map(_ % "0.4.0-M1")

//--- Projects
lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .in(file("modules/shared"))
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser",
      "io.circe" %%% "circe-refined"
    ).map(_ % "0.14.0-M4"),
    libraryDependencies ++= Seq("eu.timepit" %%% "refined").map(_ % refinedV),
    libraryDependencies ++= Seq(
      "org.scala-js"   %% "scalajs-stubs"   % "1.0.0" % "provided",
      "org.scala-js"   %% "scalajs-library" % "1.5.1",
      "org.scalatest" %%% "scalatest"       % "3.2.7" % "test"
    ),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core"   % "2.5.0",
      "org.typelevel" %%% "cats-effect" % "3.0.1",
      "org.typelevel" %%% "kittens"     % "2.2.2"
    )
  )
  .settings(scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) })

lazy val core = project
  .in(file("modules/core"))
  .dependsOn(shared.jvm)
  .settings(libraryDependencies ++= utilDeps ++ fsDeps ++ refinedDeps ++ databaseDeps)

lazy val database = project
  .in(file("modules/database"))
  .dependsOn(core)

lazy val api = project
  .in(file("api"))
  .dependsOn(database)
  .settings(libraryDependencies ++= serverDeps ++ secureDeps)

lazy val root = project
  .in(file("."))
  .aggregate(api)

//Tasks
addCommandAlias("js", ";project sharedJS; ~fastOptJS")
