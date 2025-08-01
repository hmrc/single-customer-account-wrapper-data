import sbt.*

object AppDependencies {

  private val playVersion = "play-30"
  private val bootstrapVersion = "9.18.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% s"bootstrap-backend-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc"             %% s"domain-$playVersion"             % "12.1.0",
    "uk.gov.hmrc"             %% s"play-partials-$playVersion"      % "10.1.0",
    "org.typelevel"           %% "cats-core"                        % "2.13.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% s"bootstrap-test-$playVersion"     % bootstrapVersion
  ).map(_ % Test)
}
