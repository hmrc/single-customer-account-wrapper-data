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
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Seconds, Span}
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.HttpClientSupport
import utils.WireMockHelper

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps


class MessageConnectorSpec extends AsyncWordSpec with Matchers with WireMockHelper with HttpClientSupport with MockitoSugar with ScalaFutures {

  import MessageConnectorSpec._

  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  override protected def portConfigKeys: String = "microservice.services.secure-message.port"

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

    "return None if there is an exception" in {

      server.stubFor(
        get(urlEqualTo(unreadMessageCountUrl))
          .willReturn(
            badRequest()
              .withHeader("Content-Type", "application/json")
              .withBody("invalid body")
          )
      )
      messageConnector.getUnreadMessageCount.map { response =>
        response mustBe None
      }

    }
  }

  "The messageConnector with default settings" must {
    "trigger a timeout if the request for get unread message count if it takes more than 1 second" in {

      server.stubFor(
        get(anyUrl()).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody("You haven't seen me, right!")
            .withFixedDelay(2000)
        )
      )


      implicit val patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(3, Seconds))

      val result = messageConnector.getUnreadMessageCount(scala.concurrent.ExecutionContext.global, HeaderCarrier())

      result.isReadyWithin(1 second) mustBe false
      whenReady(result) { res =>
        res mustBe None
      }
    }
  }

}

object MessageConnectorSpec {
  private val unreadMessageCountUrl = s"/secure-messaging/messages/count?taxIdentifiers=nino"
}
