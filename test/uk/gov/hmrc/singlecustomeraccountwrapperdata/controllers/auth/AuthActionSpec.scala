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
import play.api.Application
import play.api.http.Status.SEE_OTHER
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.{redirectLocation, _}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.domain.{Generator, Nino, SaUtrGenerator}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers.actions.{AuthAction, AuthActionImpl}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.fixtures.BaseSpec
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.auth.AuthenticatedRequest
import uk.gov.hmrc.singlecustomeraccountwrapperdata.utils.EnrolmentsHelper
import uk.gov.hmrc.singlecustomeraccountwrapperdata.utils.RetrievalOps._
import scala.concurrent.Future
import scala.util.Random

class AuthActionSpec extends BaseSpec {

  override implicit lazy val app: Application = GuiceApplicationBuilder()
    .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
    .configure(Map("metrics.enabled" -> false))
    .build()

  val mockAuthConnector = mock[AuthConnector]
  def controllerComponents: ControllerComponents = app.injector.instanceOf[ControllerComponents]

  class Harness(authAction: AuthAction) extends InjectedController {
    def onPageLoad: Action[AnyContent] = authAction { request: AuthenticatedRequest[AnyContent] =>
      Ok(
        s"Nino: ${request.nino.getOrElse("fail").toString}, Enrolments: ${request.enrolments.toString}," +
          s"trustedHelper: ${request.trustedHelper}, profileUrl: ${request.profile}"
      )
    }
  }
  val fakeNino = Nino(new Generator(new Random()).nextNino.nino)
  val nino = fakeNino.nino
  val fakeCredentials = Credentials("foo", "bar")
  val fakeCredentialStrength = CredentialStrength.strong
  val fakeConfidenceLevel = ConfidenceLevel.L200
  val enrolmentHelper = injected[EnrolmentsHelper]

  def fakeSaEnrolments(utr: String) = Set(Enrolment("IR-SA", Seq(EnrolmentIdentifier("UTR", utr)), "Activated"))

  def retrievals(
    nino: Option[String] = Some(nino.toString),
    affinityGroup: AffinityGroup = Individual,
    saEnrolments: Enrolments = Enrolments(Set.empty),
    credentialStrength: String = CredentialStrength.strong,
    confidenceLevel: ConfidenceLevel = ConfidenceLevel.L200,
    trustedHelper: Option[TrustedHelper] = None,
    profileUrl: Option[String] = None
  ): Harness = {

    when(mockAuthConnector.authorise[AuthRetrievals](any(), any())(any(), any())) thenReturn Future.successful(
      nino ~ affinityGroup ~ saEnrolments ~ Some(fakeCredentials) ~ Some(
        credentialStrength
      ) ~ confidenceLevel ~ None ~ trustedHelper ~ profileUrl
    )

    val authAction =
      new AuthActionImpl(mockAuthConnector, controllerComponents)

    new Harness(authAction)
  }

  val ivRedirectUrl =
    "http://localhost:9948/iv-stub/uplift?origin=PERTAX&confidenceLevel=200&completionURL=http%3A%2F%2Flocalhost%3A9232%2Fpersonal-account%2Fidentity-check-complete%3FcontinueUrl%3D%252Fpersonal-account&failureURL=http%3A%2F%2Flocalhost%3A9232%2Fpersonal-account%2Fidentity-check-complete%3FcontinueUrl%3D%252Fpersonal-account"

  "A user without a L200 confidence level must" when {
    "be redirected to the IV uplift endpoint when" must {
      "the user is an Individual" in {

        val controller = retrievals(confidenceLevel = ConfidenceLevel.L50)
        val result = controller.onPageLoad(FakeRequest("GET", "/personal-account"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must endWith(ivRedirectUrl)
      }

      "the user is an Organisation" in {

        val controller = retrievals(affinityGroup = Organisation, confidenceLevel = ConfidenceLevel.L50)
        val result = controller.onPageLoad(FakeRequest("GET", "/personal-account"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must endWith(ivRedirectUrl)
      }

      "the user is an Agent" in {

        val controller = retrievals(affinityGroup = Agent, confidenceLevel = ConfidenceLevel.L50)
        val result = controller.onPageLoad(FakeRequest("GET", "/personal-account"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must endWith(ivRedirectUrl)
      }
    }
  }

  "A user without a credential strength of Strong must" when {
    "be redirected to the MFA uplift endpoint when" must {

      val mfaRedirectUrl =
        Some(
          "http://localhost:9553/bas-gateway/uplift-mfa?origin=PERTAX&continueUrl=http%3A%2F%2Flocalhost%3A9232%2Fpersonal-account"
        )

      "the user in an Individual" in {

        val controller = retrievals(credentialStrength = CredentialStrength.weak)
        val result = controller.onPageLoad(FakeRequest("GET", "/personal-account"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe mfaRedirectUrl
      }

      "the user in an Organisation" in {

        val controller = retrievals(affinityGroup = Organisation, credentialStrength = CredentialStrength.weak)
        val result = controller.onPageLoad(FakeRequest("GET", "/personal-account"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe
          mfaRedirectUrl
      }

      "the user in an Agent" in {

        val controller = retrievals(affinityGroup = Agent, credentialStrength = CredentialStrength.weak)
        val result = controller.onPageLoad(FakeRequest("GET", "/personal-account"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe
          mfaRedirectUrl
      }
    }
  }

  "A user with a Credential Strength of 'none' must" must {
    "be redirected to the auth provider choice page" in {
      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(IncorrectCredentialStrength()))
      val authAction =
        new AuthActionImpl(mockAuthConnector, controllerComponents)
      val controller = new Harness(authAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/foo"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result).get must endWith("/auth-login-stub")
    }
  }

  "A user with no active session must" must {
    "be redirected to the auth provider choice page if unknown provider" in {
      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(SessionRecordNotFound()))
      val authAction =
        new AuthActionImpl(mockAuthConnector, controllerComponents)
      val controller = new Harness(authAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/foo"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result).get must endWith("/auth-login-stub")
    }
  }

  "A user with insufficient enrolments must" must {
    "be redirected to the Sorry there is a problem page" in {
      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(InsufficientEnrolments()))
      val authAction =
        new AuthActionImpl(mockAuthConnector, controllerComponents)
      val controller = new Harness(authAction)
      val result = controller.onPageLoad(FakeRequest("GET", "/foo"))

      whenReady(result.failed) { ex =>
        ex mustBe an[InsufficientEnrolments]
      }
    }
  }

  "A user with nino and no SA enrolment must" must {
    "create an authenticated request" in {

      val controller = retrievals()

      val result = controller.onPageLoad(FakeRequest("", ""))
      status(result) mustBe OK
      contentAsString(result) must include(nino)
    }
  }

  "A user with no nino but an SA enrolment must" must {
    "create an authenticated request" in {

      val utr = new SaUtrGenerator().nextSaUtr.utr

      val controller = retrievals(nino = None, saEnrolments = Enrolments(fakeSaEnrolments(utr)))

      val result = controller.onPageLoad(FakeRequest("", ""))
      status(result) mustBe OK
      contentAsString(result) must include(utr)
    }
  }

  "A user with a nino and an SA enrolment must" must {
    "create an authenticated request" in {

      val utr = new SaUtrGenerator().nextSaUtr.utr

      val controller = retrievals(saEnrolments = Enrolments(fakeSaEnrolments(utr)))

      val result = controller.onPageLoad(FakeRequest("", ""))
      status(result) mustBe OK
      contentAsString(result) must include(nino)
      contentAsString(result) must include(utr)
    }
  }

  "A user with trustedHelper must" must {
    "create an authenticated request containing the trustedHelper" in {

      val fakePrincipalNino = fakeNino.toString()

      val controller =
        retrievals(trustedHelper = Some(TrustedHelper("principalName", "attorneyName", "returnUrl", fakePrincipalNino)))

      val result = controller.onPageLoad(FakeRequest("", ""))
      status(result) mustBe OK
      contentAsString(result) must include(
        s"Some(TrustedHelper(principalName,attorneyName,returnUrl,$fakePrincipalNino))"
      )
    }
  }

  "A user with a SCP Profile Url must include a redirect uri back to the home controller" in {
    val controller = retrievals(profileUrl = Some("http://www.google.com/"))

    val result = controller.onPageLoad(FakeRequest("", ""))
    status(result) mustBe OK
    contentAsString(result) must include(s"http://www.google.com/?redirect_uri=")
  }

  "A user without a SCP Profile Url must continue to not have one" in {
    val controller = retrievals(profileUrl = None)

    val result = controller.onPageLoad(FakeRequest("", ""))
    status(result) mustBe OK
    contentAsString(result) mustNot include(s"http://www.google.com/?redirect_uri=")
  }

  "A user with a SCP Profile Url that is not valid must strip out the SCP Profile Url" in {
    val controller = retrievals(profileUrl = Some("notAUrl"))

    val result = controller.onPageLoad(FakeRequest("", ""))
    status(result) mustBe OK
    contentAsString(result) mustNot include("aaa")
  }
}
