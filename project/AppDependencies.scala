import sbt.*

object AppDependencies {

  private val playVersion = "play-30"
  private val bootstrapVersion = "10.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% s"bootstrap-backend-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc" %% s"domain-$playVersion" % "13.0.0",
    "uk.gov.hmrc" %% s"play-partials-$playVersion" % "10.2.0",
    "org.typelevel" %% "cats-core" % "2.13.0",
    "uk.gov.hmrc" %% s"mongo-feature-toggles-client-$playVersion" % "2.5.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% s"bootstrap-test-$playVersion" % bootstrapVersion,
    "uk.gov.hmrc" %% s"domain-test-$playVersion" % "13.0.0"
  ).map(_ % Test)
}
