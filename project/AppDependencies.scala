import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

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
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapVersion            % "test, it",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % hmrcMongoVersion            % Test,
    "org.mockito"             %% "mockito-scala"              % "1.17.7"                    % "test, it",
    "com.github.tomakehurst"   % "wiremock-standalone"        % "2.27.2"                    % "test, it",
  )
}
