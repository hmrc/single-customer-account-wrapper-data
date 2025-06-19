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

package uk.gov.hmrc.singlecustomeraccountwrapperdata

import play.api.Application
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, writeableOf_AnyContentAsEmpty}
import uk.gov.hmrc.http.HttpVerbs.GET
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.singlecustomeraccountwrapperdata.helpers.IntegrationSpec

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class ScaWrapperControllerISpec extends IntegrationSpec {

  override implicit lazy val app: Application = localGuiceApplicationBuilder()
    .configure()
    .build()

  implicit lazy val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  "ScaWrapperController" must {
    "return a 200 wrapper data response" in {

      val url = "/single-customer-account-wrapper-data/wrapper-data?lang=en&version=1.1"

      def request: FakeRequest[AnyContentAsEmpty.type] = {
        val uuid = UUID.randomUUID().toString
        FakeRequest(GET, url)
          .withSession(SessionKeys.authToken -> "Bearer 1", SessionKeys.sessionId -> s"session-$uuid")
      }

      val result: Future[Result] = route(app, request).get

      whenReady(result) { res =>
        res.header.status mustBe 200
      }
    }

    "return a 200 wrapper data response given a different wrapper library version" in {

      val url = "/single-customer-account-wrapper-data/wrapper-data?lang=en&version=0.1"

      def request: FakeRequest[AnyContentAsEmpty.type] = {
        val uuid = UUID.randomUUID().toString
        FakeRequest(GET, url)
          .withSession(SessionKeys.authToken -> "Bearer 1", SessionKeys.sessionId -> s"session-$uuid")
      }

      val result: Future[Result] = route(app, request).get

      whenReady(result) { res =>
        res.header.status mustBe 200
      }
    }

    "return a 400 given missing parameters in the url" in {

      val url = "/single-customer-account-wrapper-data/wrapper-data"

      def request: FakeRequest[AnyContentAsEmpty.type] = {
        val uuid = UUID.randomUUID().toString
        FakeRequest(GET, url)
          .withSession(SessionKeys.authToken -> "Bearer 1", SessionKeys.sessionId -> s"session-$uuid")
      }

      val result: Future[Result] = route(app, request).get

      whenReady(result) { res =>
        res.header.status mustBe 400
      }
    }
  }
}
