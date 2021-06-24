import com.typesafe.sbt.packager.MappingsHelper.directory

ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "2.13.5"
ThisBuild / scalacOptions := Seq(
  "-language:postfixOps",
  "-Ymacro-annotations"
)

//--- Dependencies
val fs2V        = "3.0.1"
val refinedV    = "0.9.23"
val refinedDeps = Seq("eu.timepit" %% "refined-pureconfig").map(_ % refinedV)

val utilDeps = Seq(
  "ch.qos.logback"         % "logback-classic" % "1.2.3",
  "com.github.pureconfig" %% "pureconfig"      % "0.14.1",
  "co.fs2"                %% "fs2-io"          % fs2V
)

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
    ),
    libraryDependencies += "co.fs2" %%% "fs2-core" % fs2V
  )

lazy val core = project
  .in(file("modules/core"))
  .dependsOn(shared.jvm)
  .settings(libraryDependencies ++= utilDeps ++ refinedDeps ++ databaseDeps)

lazy val database = project
  .in(file("modules/database"))
  .dependsOn(core)

lazy val api = project
  .in(file("api"))
  .dependsOn(database)
  .settings(libraryDependencies ++= serverDeps ++ secureDeps)
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings(
    dockerBaseImage := "openjdk:11",
    dockerRepository := Some("vovapetryna"),
    Docker / packageName := "easy",
    dockerExposedPorts := 9001 :: 9001 :: Nil,
    Universal / mappings ++= directory(target.value / "../../app/src/main/react_app/build")
  )

lazy val copyTask = taskKey[Unit]("ScalaJS copy bundle to app")
lazy val client = project
  .in(file("modules/client"))
  .dependsOn(shared.js)
  .settings(libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0")
  .enablePlugins(ScalaJSPlugin)
  .settings(scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) })
  .settings(
    copyTask := {
      import java.nio.file
      def copyToDir(filePathName: file.Path, dirName: file.Path) = {
        file.Files.copy(filePathName, dirName, file.StandardCopyOption.REPLACE_EXISTING)
      }
      (Compile / fastOptJS).value
      val fileName  = "client-fastopt.js"
      val inputFile = ((Compile / target).value / s"scala-2.13/$fileName").toPath
      val targetDir = ((Compile / baseDirectory).value / s"../../app/src/main/public-scala-bundle/$fileName").toPath
      copyToDir(inputFile, targetDir)
    }
  )

lazy val root = project
  .in(file("."))
  .aggregate(api)

//Tasks
addCommandAlias("js", ";project client; fastOptJS; copyTask")
addCommandAlias("docker", ";project api; docker:publishLocal")
