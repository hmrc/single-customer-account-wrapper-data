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

package uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.ws.WSClient
import play.api.mvc.AnyContent
import play.api.test.Helpers
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status}
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name}
import uk.gov.hmrc.auth.core.{AuthConnector, ConfidenceLevel, CredentialStrength, Enrolments}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.{AppConfig, WrapperConfig}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers.actions.AuthActionImpl
import uk.gov.hmrc.singlecustomeraccountwrapperdata.fixtures.BaseSpec
import uk.gov.hmrc.singlecustomeraccountwrapperdata.fixtures.RetrievalOps.Ops
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.MenuItemConfig
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.auth.AuthenticatedRequest

import scala.concurrent.Future

class ScaWrapperControllerSpec extends BaseSpec {

  lazy val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
  lazy val appConfig: AppConfig = injector.instanceOf[AppConfig]

  lazy val wrapperConfig: WrapperConfig = new WrapperConfig(appConfig)(messagesApi) {
    override def fallbackMenuConfig()(implicit request: AuthenticatedRequest[AnyContent], lang: Lang): Seq[MenuItemConfig] = {
      Seq(
        MenuItemConfig("Fallback1", s"${appConfig.pertaxUrl}", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None),
        MenuItemConfig("Fallback2", s"${appConfig.pertaxUrl}/messages", leftAligned = false, position = 0, None, None),
        MenuItemConfig("Fallback3", s"${appConfig.pertaxUrl}/track", leftAligned = false, position = 1, None, None),
        MenuItemConfig("Fallback4", s"${appConfig.pertaxUrl}/profile-and-settings", leftAligned = false, position = 2, None, None),
        MenuItemConfig("Fallback5", s"${appConfig.defaultSignoutUrl}", leftAligned = false, position = 4, None, None, signout = true)
      )
    }
  }

  override implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  lazy val authAction = new AuthActionImpl(mockAuthConnector, messagesControllerComponents)

  private val controller = new ScaWrapperController(Helpers.stubControllerComponents(), appConfig, wrapperConfig, authAction)
  private val wsClient = app.injector.instanceOf[WSClient]
  private val baseUrl = "http://localhost:8422/single-customer-account-wrapper-data/wrapper-data/:version"
  val nino = "AA999999A"

  wsClient.url(baseUrl).withHttpHeaders("Authorization" -> "Bearer123").get()
  when(mockAuthConnector.authorise[AuthRetrievals](any(), any())(any(), any())) thenReturn Future.successful(
    Some(nino) ~
      Individual ~
      Enrolments(fakeSaEnrolments("11111111", "Activated")) ~
      Some(Credentials("id", "type")) ~
      Some(CredentialStrength.strong) ~
      ConfidenceLevel.L200 ~
      Some(Name(Some("chaz"), Some("dingle"))) ~
      Some(TrustedHelper("name", "name", "link", "AA999999A")) ~
      Some("profileUrl")
  )

  "The Wrapper data API" must {
    "return the normal menu config when wrapper-data and sca-wrapper are the same versions" in {
      val version: String = appConfig.versionNum
      val lang: String = "en"
      val result = controller.wrapperData(lang, version)(fakeRequest)
      status(result) shouldBe 200
      contentAsString(result).contains("Profile and settings") mustBe true
    }

    "return the fallback menu config when wrapper-data and sca-wrapper are not the same versions" in {

      val version: String = "0.0.1"
      val lang: String = "en"
      val result = controller.wrapperData(lang, version)(fakeRequest)
      status(result) shouldBe 200
      contentAsString(result).contains("Fallback1") mustBe true
    }
  }
}
