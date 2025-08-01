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

package uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name}
import uk.gov.hmrc.auth.core.{AuthConnector, ConfidenceLevel, CredentialStrength, Enrolments}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.{AppConfig, UrBannersConfig, WebchatConfig, WrapperConfig}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.connectors.{FandFConnector, MessageConnector}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers.actions.AuthActionImpl
import uk.gov.hmrc.singlecustomeraccountwrapperdata.fixtures.BaseSpec
import uk.gov.hmrc.singlecustomeraccountwrapperdata.fixtures.RetrievalOps.Ops

import scala.concurrent.Future

class ScaWrapperWithMessagesControllerSpec extends BaseSpec with Matchers with BeforeAndAfterEach {

  lazy val messagesApi: MessagesApi          = injector.instanceOf[MessagesApi]
  lazy val appConfig: AppConfig              = injector.instanceOf[AppConfig]
  lazy val wrapperConfig: WrapperConfig      = new WrapperConfig(appConfig)(messagesApi)
  val mockAuthConnector: AuthConnector       = mock[AuthConnector]
  val mockFandFConnector: FandFConnector     = mock[FandFConnector]
  val mockBannerConfig: UrBannersConfig      = mock[UrBannersConfig]
  val mockWebchatConfig: WebchatConfig       = mock[WebchatConfig]
  val mockMessageConnector: MessageConnector = mock[MessageConnector]
  override implicit val hc: HeaderCarrier    = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))

  lazy val authAction = new AuthActionImpl(mockAuthConnector, messagesControllerComponents, mockFandFConnector)

  private val controller = new ScaWrapperWithMessagesController(
    stubControllerComponents(),
    appConfig,
    wrapperConfig,
    mockBannerConfig,
    mockWebchatConfig,
    mockMessageConnector,
    authAction
  )

  val nino = "AA999999A"

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockBannerConfig, mockWebchatConfig, mockMessageConnector, mockAuthConnector, mockFandFConnector)
    when(mockBannerConfig.getUrBannersByService).thenReturn(Map.empty)
    when(mockWebchatConfig.getWebchatUrlsByService).thenReturn(Map.empty)
    when(mockFandFConnector.getTrustedHelper()(any())).thenReturn(Future.successful(None))
    when(mockAuthConnector.authorise[AuthRetrievals](any(), any())(any(), any())) thenReturn Future.successful(
      Some(nino) ~
        Some(Individual) ~
        Enrolments(Set.empty) ~
        Some(Credentials("id", "type")) ~
        Some(CredentialStrength.strong) ~
        ConfidenceLevel.L200 ~
        Some(Name(Some("chaz"), Some("dingle"))) ~
        Some("profileUrl")
    )
  }

  "The Wrapper data with messages API" must {

    "return wrapper data with unreadMessageCount when available" in {
      when(mockMessageConnector.getUnreadMessageCount(any(), any())).thenReturn(Future.successful(Some(3)))

      val result = controller.wrapperDataWithMessages("en", appConfig.versionNum)(fakeRequest)
      status(result) mustBe OK
      val json   = Json.parse(contentAsString(result))
      (json \ "unreadMessageCount").as[Int] mustBe 3
    }

    "return wrapper data with no unreadMessageCount when no messages are unread" in {
      when(mockMessageConnector.getUnreadMessageCount(any(), any())).thenReturn(Future.successful(None))

      val result = controller.wrapperDataWithMessages("en", appConfig.versionNum)(fakeRequest)
      status(result) mustBe OK
      val json   = Json.parse(contentAsString(result))
      (json \ "unreadMessageCount").isEmpty mustBe true
    }

    "return wrapper data gracefully when message connector fails" in {
      when(mockMessageConnector.getUnreadMessageCount(any(), any()))
        .thenReturn(Future.failed(new RuntimeException("error")))

      val result = controller.wrapperDataWithMessages("en", appConfig.versionNum)(fakeRequest)
      status(result) mustBe OK
      val json   = Json.parse(contentAsString(result))
      (json \ "unreadMessageCount").isEmpty mustBe true
    }
  }

  "return wrapper data gracefully when UpstreamErrorResponse has client error (400–497)" in {
    val ex = UpstreamErrorResponse("Client Error", 404, 404)
    when(mockMessageConnector.getUnreadMessageCount(any(), any()))
      .thenReturn(Future.failed(ex))

    val result = controller.wrapperDataWithMessages("en", appConfig.versionNum)(fakeRequest)
    status(result) mustBe OK
    val json   = Json.parse(contentAsString(result))
    (json \ "unreadMessageCount").isEmpty mustBe true
  }

  "return wrapper data gracefully when UpstreamErrorResponse has server error (≥ 499)" in {
    val ex = UpstreamErrorResponse("Server Error", 503, 503)
    when(mockMessageConnector.getUnreadMessageCount(any(), any()))
      .thenReturn(Future.failed(ex))

    val result = controller.wrapperDataWithMessages("en", appConfig.versionNum)(fakeRequest)
    status(result) mustBe OK
    val json   = Json.parse(contentAsString(result))
    (json \ "unreadMessageCount").isEmpty mustBe true
  }
}
