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

package uk.gov.hmrc.singlecustomeraccountwrapperdata.connectors

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.http.Fault
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.retrieve.v2.TrustedHelper
import uk.gov.hmrc.domain.Generator
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.HttpClientV2Support
import utils.WireMockHelper

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class FandFConnectorSpec
    extends AsyncWordSpec
    with Matchers
    with WireMockHelper
    with HttpClientV2Support
    with MockitoSugar
    with ScalaFutures {

  override protected def portConfigKeys: String = "microservice.services.fandf.port"

  private val trustedHelperNino = new Generator().nextNino

  val trustedHelper: TrustedHelper =
    TrustedHelper("principal Name", "attorneyName", "returnLink", Some(trustedHelperNino.nino))

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val fandfTrustedHelperResponse: String =
    s"""
       |{
       |   "principalName": "principal Name",
       |   "attorneyName": "attorneyName",
       |   "returnLinkUrl": "returnLink",
       |   "principalNino": "$trustedHelperNino"
       |}
       |""".stripMargin

  lazy val connector: FandFConnector = app.injector.instanceOf[FandFConnector]

  "Calling FandFConnector.getTrustedHelper" must {

    "return as Some(trustedHelper) when trustedHelper json returned" in {
      server.stubFor(
        WireMock.get(urlEqualTo("/delegation/get")).willReturn(ok(fandfTrustedHelperResponse))
      )

      val result: Option[TrustedHelper] = Await.result(connector.getTrustedHelper(), Duration.Inf)

      result mustBe Some(trustedHelper)
    }

    "return as Some(trustedHelper) when invalid json returned" in {
      server.stubFor(
        WireMock.get(urlEqualTo("/delegation/get")).willReturn(ok("Nonsense response"))
      )

      val result: Option[TrustedHelper] = Await.result(connector.getTrustedHelper(), Duration.Inf)

      result mustBe None
    }

    "return as None when not found returned" in {
      server.stubFor(
        WireMock.get(urlEqualTo("/delegation/get")).willReturn(notFound())
      )

      val result: Option[TrustedHelper] = Await.result(connector.getTrustedHelper(), Duration.Inf)

      result mustBe None
    }

    "return None when error status returned" in {
      server.stubFor(
        WireMock.get(urlEqualTo("/delegation/get")).willReturn(serverError())
      )
      val result = Await.result(connector.getTrustedHelper(), Duration.Inf)

      result mustBe None
    }

    "return None when unexpected status returned" in {
      server.stubFor(
        WireMock.get(urlEqualTo("/delegation/get")).willReturn(noContent())
      )
      val result = Await.result(connector.getTrustedHelper(), Duration.Inf)

      result mustBe None
    }

    "return None when there is an exception" in {
      server.stubFor(
        WireMock
          .get(urlEqualTo("/delegation/get"))
          .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE))
      )
      val result = Await.result(connector.getTrustedHelper(), Duration.Inf)

      result mustBe None
    }
  }
}
