/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.singlecustomeraccountwrapperdata.fixtures

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, BodyParsers, MessagesControllerComponents}
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name, ~}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.AppConfig
import uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers.actions.AuthAction

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{FiniteDuration, _}
import scala.reflect.ClassTag

trait BaseSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with PatienceConfiguration
    with Injecting
    with MockitoSugar
    with GuiceOneAppPerSuite
    with BeforeAndAfterEach {

  // override lazy val app: Application = applicationBuilder().build()
  implicit val system: ActorSystem                   = ActorSystem("Test")
  implicit val materializer: Materializer            = Materializer(system)
  lazy val injector: Injector                        = app.injector
  def injected[T](implicit evidence: ClassTag[T]): T = app.injector.instanceOf[T]

  override implicit lazy val app: Application = GuiceApplicationBuilder().build()

  implicit val defaultTimeout: FiniteDuration       = 5.seconds
  implicit val hc: HeaderCarrier                    = HeaderCarrier()
  implicit lazy val ec: ExecutionContext            = app.injector.instanceOf[ExecutionContext]
  implicit val frontendAppConfigInstance: AppConfig = injector.instanceOf[AppConfig]

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type]           = FakeRequest("", "")
    .withSession(SessionKeys.sessionId -> "foo")
    .withCSRFToken
    .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
  lazy val messagesApiInstance: MessagesApi                           = injector.instanceOf[MessagesApi]
  // lazy val messages: Messages = messagesApiInstance.preferred(fakeRequest)
  lazy val messagesControllerComponents: MessagesControllerComponents =
    injector.instanceOf[MessagesControllerComponents]
  lazy val authActionInstance: AuthAction                             = injector.instanceOf[AuthAction]
  lazy val bodyParserInstance: BodyParsers.Default                    = injector.instanceOf[BodyParsers.Default]
  lazy val FakeAuthAction: AuthAction                                 = mock[AuthAction]

  type AuthRetrievals =
    Option[String] ~ Option[AffinityGroup] ~ Enrolments ~ Option[Credentials] ~ Option[String] ~ ConfidenceLevel ~
      Option[Name] ~ Option[String]

  def fakeSaEnrolments(utr: String, enrolmentState: String): Set[Enrolment] = Set(
    Enrolment("IR-SA", Seq(EnrolmentIdentifier("UTR", utr)), enrolmentState)
  )

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

}
