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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.HttpClientSupport
import utils.WireMockHelper


class MessageConnectorSpec extends AsyncWordSpec with Matchers with WireMockHelper with HttpClientSupport with MockitoSugar {

  import MessageConnectorSpec._

  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  override protected def portConfigKeys: String = "microservice.services.message-frontend.port"

  private lazy val messageConnector: MessageConnector = injector.instanceOf[MessageConnector]

  "getUnreadMessageCount" must {
    "return the unread message count if more than zero" in {
      val messageResponse: JsObject = Json.obj(
        "count" -> Json.obj(
          "total" -> 2,
          "unread" -> 1
        )
      )

      server.stubFor(
        get(urlEqualTo(unreadMessageCountUrl))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(messageResponse.toString())
          )
      )
      messageConnector.getUnreadMessageCount.map { response =>
        response mustBe Some(1)
      }
    }

    "return None if the unread message count equal to zero" in {
      val messageResponse: JsObject = Json.obj(
        "count" -> Json.obj(
          "total" -> 2,
          "unread" -> 0
        )
      )
      server.stubFor(
        get(urlEqualTo(unreadMessageCountUrl))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(messageResponse.toString())
          )
      )
      messageConnector.getUnreadMessageCount.map { response =>
        response mustBe None
      }
    }

    "return None if the unread message count is less than zero" in {
      val messageResponse: JsObject = Json.obj(
        "count" -> Json.obj(
          "total" -> 2,
          "unread" -> -1
        )
      )
      server.stubFor(
        get(urlEqualTo(unreadMessageCountUrl))
          .willReturn(
            ok
              .withHeader("Content-Type", "application/json")
              .withBody(messageResponse.toString())
          )
      )
      messageConnector.getUnreadMessageCount.map { response =>
        response mustBe None
      }
    }
  }
}

object MessageConnectorSpec {
  private val unreadMessageCountUrl = s"/messages?countOnly=true"
}