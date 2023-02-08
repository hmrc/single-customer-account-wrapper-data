import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._
import play.core.PlayVersion.current

object AppDependencies {

  private val bootstrapVersion = "7.0.0"
  private val hmrcMongoVersion = "0.70.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
    "uk.gov.hmrc"             %% "domain"                     % s"8.0.0-play-28",
    "uk.gov.hmrc"             %% "play-partials"              % "8.3.0-play-28",
    "org.typelevel"           %% "cats-core"                  % "2.8.0"
  )

  val test = Seq(
    "org.scalatest"           %% "scalatest"           % "3.2.8",
    "com.typesafe.play"       %% "play-test"           % current,
    "org.scalatestplus.play"  %% "scalatestplus-play"  % "4.0.3",
    "org.scalatestplus"       %% "mockito-3-4"         % "3.2.3.0",
    "org.mockito"             % "mockito-core"         % "3.6.28",
    "org.scalacheck"          %% "scalacheck"          % "1.15.1",
    "com.github.tomakehurst"  % "wiremock-standalone"  % "2.27.2",
    "com.vladsch.flexmark"    % "flexmark-all"         % "0.36.8"
  ).map(_ % "test,it")
}
