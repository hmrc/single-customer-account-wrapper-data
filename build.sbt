import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import scoverage.ScoverageKeys

lazy val microservice = Project("single-customer-account-wrapper-data", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
        majorVersion        := 0,
        scalaVersion        := "2.13.8",
        libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
        // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
        // suppress warnings in generated routes files
        scalacOptions += "-Wconf:src=routes/.*:s",
        PlayKeys.playDefaultPort := 8422,
        ScoverageKeys.coverageMinimumStmtTotal := 90,
        ScoverageKeys.coverageFailOnMinimum := true,
        ScoverageKeys.coverageHighlighting := true,
        Test / coverageEnabled := true
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)
