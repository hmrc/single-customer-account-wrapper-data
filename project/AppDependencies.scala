import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._
import play.core.PlayVersion.current

object AppDependencies {

  private val bootstrapVersion = "7.14.0"
  private val hmrcMongoVersion = "0.74.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
    "uk.gov.hmrc"             %% "domain"                     % s"8.1.0-play-28",
    "uk.gov.hmrc"             %% "play-partials"              % "8.4.0-play-28",
    "org.typelevel"           %% "cats-core"                  % "2.9.0"
  )

  val test = Seq(
    "org.scalatest"           %% "scalatest"           % "3.2.15",
    "com.typesafe.play"       %% "play-test"           % current,
    "org.scalatestplus.play"  %% "scalatestplus-play"  % "4.0.3",
    "org.scalatestplus"       %% "mockito-3-4"         % "3.2.10.0",
    "org.mockito"             %% "mockito-scala"       % "1.17.12",
    "org.scalacheck"          %% "scalacheck"          % "1.17.0",
    "com.github.tomakehurst"  % "wiremock-standalone"  % "2.27.2",
    "com.vladsch.flexmark"    % "flexmark-all"         % "0.62.2"
  ).map(_ % "test,it")
}
