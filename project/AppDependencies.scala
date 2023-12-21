import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.1.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "uk.gov.hmrc"             %% s"domain-play-30"                  % "9.0.0",
    "uk.gov.hmrc"             %% s"play-partials-play-30"           % "9.1.0",
    "org.typelevel"           %% "cats-core"                  % "2.10.0"
  )

  val test = Seq(
    "uk.gov.hmrc"                   %% "bootstrap-test-play-30"  % bootstrapVersion,
    "com.fasterxml.jackson.module"  %% "jackson-module-scala"    % "2.14.2"
  ).map(_ % Test)
}
