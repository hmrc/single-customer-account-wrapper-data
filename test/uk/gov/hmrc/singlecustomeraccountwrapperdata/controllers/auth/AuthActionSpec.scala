/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers.auth

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.domain.{Generator, Nino, SaUtrGenerator}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers.actions.{AuthAction, AuthActionImpl}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.fixtures.BaseSpec
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.auth.AuthenticatedRequest
import uk.gov.hmrc.singlecustomeraccountwrapperdata.utils.RetrievalOps._

import scala.concurrent.Future
import scala.util.Random

class AuthActionSpec extends BaseSpec {

  override implicit lazy val app: Application = GuiceApplicationBuilder()
//    .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
    .build()

  val mockAuthConnector                          = mock[AuthConnector]
  def controllerComponents: ControllerComponents = app.injector.instanceOf[ControllerComponents]

  class Harness(authAction: AuthAction) extends InjectedController {
    def onPageLoad: Action[AnyContent] = authAction { request: AuthenticatedRequest[AnyContent] =>
      Ok(
        s"Nino: ${request.nino.getOrElse("fail").toString}, Enrolments: ${request.enrolments.toString}," +
          s"trustedHelper: ${request.trustedHelper}, profileUrl: ${request.profile}"
      )
    }
  }
  val fakeNino               = Nino(new Generator(new Random()).nextNino.nino)
  val nino                   = fakeNino.nino
  val fakeCredentials        = Credentials("foo", "bar")
  val fakeCredentialStrength = CredentialStrength.strong
  val fakeConfidenceLevel    = ConfidenceLevel.L200

  def fakeSaEnrolments(utr: String) = Set(Enrolment("IR-SA", Seq(EnrolmentIdentifier("UTR", utr)), "Activated"))

  def retrievals(
    nino: Option[String] = Some(nino.toString),
    affinityGroup: AffinityGroup = Individual,
    saEnrolments: Enrolments = Enrolments(Set.empty),
    credentialStrength: String = CredentialStrength.strong,
    confidenceLevel: ConfidenceLevel = ConfidenceLevel.L200,
    trustedHelper: Option[TrustedHelper] = None,
    profileUrl: Option[String] = None,
    exception: Option[AuthorisationException] = None
  ): Harness = {
    if (exception.isDefined) {
      when(mockAuthConnector.authorise[AuthRetrievals](any(), any())(any(), any())) thenReturn Future.failed(
        exception.get
      )
    } else {
      when(mockAuthConnector.authorise[AuthRetrievals](any(), any())(any(), any())) thenReturn Future.successful(
        nino ~ affinityGroup ~ saEnrolments ~ Some(fakeCredentials) ~ Some(
          credentialStrength
        ) ~ confidenceLevel ~ None ~ trustedHelper ~ profileUrl
      )
    }

    val authAction =
      new AuthActionImpl(mockAuthConnector, controllerComponents)

    new Harness(authAction)
  }

  "An authenticated request" must {
    "be created when a user has a nino and SA enrolment" in {
      val utr = new SaUtrGenerator().nextSaUtr.utr

      val controller = retrievals(saEnrolments = Enrolments(fakeSaEnrolments(utr)))

      val result = controller.onPageLoad(FakeRequest("", ""))
      status(result) mustBe OK
      contentAsString(result) must include(nino)
      contentAsString(result) must include(utr)
    }

    "be created when a user has a nino and SA enrolment and trusted helper" in {
      val utr = new SaUtrGenerator().nextSaUtr.utr

      val controller = retrievals(
        saEnrolments = Enrolments(fakeSaEnrolments(utr)),
        trustedHelper = Some(TrustedHelper("chaz", "dingle", "link", nino))
      )

      val result = controller.onPageLoad(FakeRequest("", ""))
      status(result) mustBe OK
      contentAsString(result) must include(nino)
      contentAsString(result) must include(utr)
      contentAsString(result) must include("chaz")
    }

    "be created when a user has a nino and no enrolments" in {

      val controller = retrievals()

      val result = controller.onPageLoad(FakeRequest("", ""))
      status(result) mustBe OK
      contentAsString(result) must include(nino)
    }
  }

  "An unauthenticated request" must {
    "be created when a user has less than 200 CL" in {

      val controller = retrievals(confidenceLevel = ConfidenceLevel.L50)

      val result = controller.onPageLoad(FakeRequest("", ""))
      status(result) mustBe OK
      contentAsString(result) must include(nino)
    }

    "be created when a user has more than 50 CL but has a weak cred strength" in {

      val controller = retrievals(confidenceLevel = ConfidenceLevel.L200, credentialStrength = CredentialStrength.weak)

      val result = controller.onPageLoad(FakeRequest("", ""))
      status(result) mustBe OK
      contentAsString(result) must include("fail")
    }

    "be created when a user has a weak cred strength" in {

      val controller = retrievals()

      val result = controller.onPageLoad(FakeRequest("", ""))
      status(result) mustBe OK
      contentAsString(result) must include(nino)
    }

    "be created when an auth exception occurs" in {

      val controller = retrievals(exception = Some(MissingBearerToken("error")))

      val result = controller.onPageLoad(FakeRequest("", ""))
      status(result) mustBe OK
      contentAsString(result) must include("fail")
    }
  }

}
