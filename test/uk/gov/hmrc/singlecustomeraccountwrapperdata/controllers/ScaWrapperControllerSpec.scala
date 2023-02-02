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
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import play.api.libs.ws.{WSClient}
import play.api.mvc.{Result}
import play.api.test.{Helpers}
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.{AuthConnector, ConfidenceLevel, CredentialStrength, Enrolments}
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.AppConfig
import uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers.actions.{AuthAction, AuthActionImpl}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.fixtures.RetrievalOps.Ops
import uk.gov.hmrc.singlecustomeraccountwrapperdata.fixtures.SpecBase

import scala.concurrent.{ExecutionContext, Future}

class ScaWrapperControllerSpec extends SpecBase {


      lazy val appConfig: AppConfig = injector.instanceOf[AppConfig]
      implicit val hc = HeaderCarrier(authorization = Some(Authorization("Bearer 123")))
      val mockAuthConnector: AuthConnector = mock[AuthConnector]
      lazy val authAction = new AuthActionImpl(mockAuthConnector, bodyParserInstance)

      private val controller = new ScaWrapperController(Helpers.stubControllerComponents(), appConfig, authAction)
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

  "test to check the version control" must {
    "test to check the menu configuration with same versions" in {

      val version: String = "1.0.0"
      val result = controller.wrapperData(version)(fakeRequest)
      whenReady(result) { res =>
        res.header.status shouldBe 200
      }
      contentAsString(result).contains("Profile and settings") mustBe true
    }

    "test to check the menu configuration with different versions" in {
      val version: String = "2.0.0"
      val result: Future[Result] = controller.wrapperData(version)(fakeRequest)
      whenReady(result) { res =>
        res.header.status shouldBe 200
      }
      //contentAsString(result).contains("Profile and settings") mustBe false
    }


  }
}