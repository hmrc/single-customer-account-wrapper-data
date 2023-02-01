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

package uk.gov.hmrc.singlecustomeraccountwrapperdata.connectors

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, ok, serverError, stubFor, urlEqualTo}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.partials.HeaderCarrierForPartialsConverter
import uk.gov.hmrc.singlecustomeraccountwrapperdata.connectors.{HttpClientResponse, ScaWrapperMessageConnector}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers.actions.AuthAction
import uk.gov.hmrc.singlecustomeraccountwrapperdata.fixtures.{SpecBase, WireMockHelper}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.services.MessageCountService

import java.util.UUID

class ScaWrapperMessageConnectorSpec extends SpecBase with WireMockHelper{

  val mockHeaderCarrierForPartialsConverter: HeaderCarrierForPartialsConverter = mock[HeaderCarrierForPartialsConverter]
  val mockScaWrapperMessageConnector: ScaWrapperMessageConnector = mock[ScaWrapperMessageConnector]
  val mockHttpClientResponse: HttpClientResponse = mock[HttpClientResponse]

  protected def applicationBuilder(): Application =
    new GuiceApplicationBuilder()
      .overrides(
        bind[ScaWrapperMessageConnector].toInstance(mockScaWrapperMessageConnector)
      )
      .configure(
        "microservice.services.message-frontend.port" -> 8080,
        "metrics.enabled" -> false,
        "auditing.enabled" -> false,
        "auditing.traceRequests" -> false
      )
      .build()

  def buildFakeRequestWithAuth(
                                method: String,
                                uri: String = "/wrapper-data/:version"
                              ): FakeRequest[AnyContentAsEmpty.type] = {
    val session = Map(
      SessionKeys.sessionId -> s"session-${UUID.randomUUID()}",
     // SessionKeys.lastRequestTimestamp -> now.getMillis.toString
    )

    FakeRequest(method, uri).withSession(session.toList: _*)
  }

 // private val fakeRequest = FakeRequest("GET", "/wrapper-data/:version")


  val messageCountService = injector.instanceOf[MessageCountService]

  "Calling getMessageCount" should {
    def messageCount = messageCountService.getUnreadMessageCount(fakeRequest)

    "return None unread messages when http client throws an exception" in {


      messageCount.futureValue mustBe None
    }

    "return None unread messages when http client does not return a usable response" in {
      server.stubFor(
        WireMock.get(urlEqualTo("/messages/count?read=No")).willReturn(ok(Json.obj("testInvalid" -> "testInvalid").toString))
      )

      messageCount.futureValue mustBe None
    }

    "return 10 unread messages" in {
      server.stubFor(WireMock.get(urlEqualTo("/messages/count?read=No"))
        .willReturn(
          aResponse()
            .withStatus(200)
            .withBody("""{"count":10}""".stripMargin)))
println("kritika" + messageCount.futureValue)
      messageCount.futureValue mustBe Some(10)
    }
  }
}
