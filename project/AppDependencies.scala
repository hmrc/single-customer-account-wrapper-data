import sbt._

object AppDependencies {

  private val playVersion = "play-30"
  private val bootstrapVersion = "8.6.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% s"bootstrap-backend-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc"             %% s"domain-$playVersion"             % "9.0.0",
    "uk.gov.hmrc"             %% s"play-partials-$playVersion"      % "9.1.0",
    "org.typelevel"           %% "cats-core"                        % "2.10.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% s"bootstrap-test-$playVersion"     % bootstrapVersion
  ).map(_ % Test)
}
