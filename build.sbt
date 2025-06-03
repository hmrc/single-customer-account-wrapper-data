import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "2.13.12"
ThisBuild / scalafmtOnCompile := true

lazy val microservice = Project("single-customer-account-wrapper-data", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions ++= Seq(
      "-unchecked",
      "-feature",
      "-Xlint:_",
      "-Werror",
      "-Wdead-code",
      "-Wunused:_",
      "-Wextra-implicit",
      "-Wconf:src=routes/.*:s"
    ),
    PlayKeys.playDefaultPort := 8422
  )
  .settings(CodeCoverageSettings.settings: _*)

Test / parallelExecution := true
Test / scalacOptions --= Seq("-Wdead-code", "-Wvalue-discard")

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
