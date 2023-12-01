import org.scalajs.linker.interface.ModuleSplitStyle

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

val fastLinkOutputDir = taskKey[String]("output directory for `npm run dev`")
val fullLinkOutputDir = taskKey[String]("output directory for `npm run build`")

lazy val app = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    name := "java-date-time-ui",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.2.0",
      "com.raquo" %%% "laminar" % "16.0.0",
      "be.doeraene" %%% "web-components-ui5" % "1.17.1",
      "io.github.cquiroz" %%% "scala-java-time" % "2.5.0",
      "io.github.cquiroz" %%% "scala-java-time-tzdb" % "2.5.0"
    ),
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("me.chadrs.datetimes")))
    },
    // Generated scala.js output will call your main() method to start your app.
    scalaJSUseMainModuleInitializer := true,
    fastLinkOutputDir := {
      // Ensure that fastLinkJS has run, then return its output directory
      (Compile / fastLinkJS).value
      (Compile / fastLinkJS / scalaJSLinkerOutputDirectory).value.getAbsolutePath
    },
    fullLinkOutputDir := {
      // Ensure that fullLinkJS has run, then return its output directory
      (Compile / fullLinkJS).value
      (Compile / fullLinkJS / scalaJSLinkerOutputDirectory).value.getAbsolutePath
    }
  )
