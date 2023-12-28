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

package uk.gov.hmrc.singlecustomeraccountwrapperdata

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.Application
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.http.HttpVerbs.GET
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.singlecustomeraccountwrapperdata.helpers.IntegrationSpec
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.{MessageCount, MessageCountResponse}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}


class ScaMessageControllerISpec
  extends IntegrationSpec {

  override implicit lazy val app: Application = localGuiceApplicationBuilder()
    .configure()
    .build()

  implicit lazy val ec = app.injector.instanceOf[ExecutionContext]

  val url = "/single-customer-account-wrapper-data/message-data"

  def request: FakeRequest[AnyContentAsEmpty.type] = {
    val uuid = UUID.randomUUID().toString
    FakeRequest(GET, url)
      .withSession(SessionKeys.authToken -> "Bearer 1", SessionKeys.sessionId -> s"session-$uuid")
  }

  "ScaMessageController" must {
    "return a 200 wrapper data response" in {

      val response = Json.toJson(MessageCountResponse(MessageCount(total = 1, unread = 1))).toString()

      server.stubFor(
        get(urlEqualTo("/secure-messaging/messages/count?taxIdentifiers=nino"))
          .willReturn(ok(response))
      )

      val result: Future[Result] = route(app, request).get

      whenReady(result) { res =>
        res.header.status mustBe 200
      }
    }

    "return a 204 given no unread messages" in {

      val response = Json.toJson(MessageCountResponse(MessageCount(total = 0, unread = 0))).toString()

      server.stubFor(
        get(urlEqualTo("/secure-messaging/messages/count?taxIdentifiers=nino"))
          .willReturn(ok(response))
      )

      val result: Future[Result] = route(app, request).get

      whenReady(result) { res =>
        res.header.status mustBe 204
      }
    }

    "return a 204 given a bad json response" in {

      val response = Json.toJson(MessageCount(total = 1, unread = 1)).toString()

      server.stubFor(
        get(urlEqualTo("/secure-messaging/messages/count?taxIdentifiers=nino"))
          .willReturn(ok(response))
      )

      val result: Future[Result] = route(app, request).get

      whenReady(result) { res =>
        res.header.status mustBe 204
      }
    }

  }
}
