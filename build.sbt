import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 2
ThisBuild / scalaVersion := "3.3.6"
ThisBuild / scalafmtOnCompile := true

val scoverageSettings: Seq[Setting[_]] = Seq(
  ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;uk.gov.hmrc.BuildInfo;app.*;prod.*;.*Routes.*;testOnly.*;testOnlyDoNotUseInAppConf.*;.*\\$anon.*",
  ScoverageKeys.coverageMinimumStmtTotal := 92,
  ScoverageKeys.coverageMinimumBranchTotal := 70,
  ScoverageKeys.coverageFailOnMinimum := true,
  ScoverageKeys.coverageHighlighting := true
)

addCommandAlias("report", ";clean; coverage; test; it/test; coverageReport")

lazy val microservice = Project("single-customer-account-wrapper-data", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions ++= Seq(
      "-feature",
      "-unchecked",
      "-language:noAutoTupling",
      "-Werror",
      "-Wconf:msg=Flag.*repeatedly:s",
      "-Wconf:msg=value name in trait Retrievals is deprecated:s",
      "-Wconf:src=routes/.*:s"
    ),
    PlayKeys.playDefaultPort := 8422,
    scoverageSettings
  )

Test / parallelExecution := true

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
