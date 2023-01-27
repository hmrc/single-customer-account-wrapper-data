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

import akka.util.Timeout
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, Result}
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.baseApplicationBuilder.injector
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, redirectLocation, route, writeableOf_AnyContentAsEmpty, status => httpStatus}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.AppConfig
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.WrapperDataRequest

import java.util.concurrent.TimeUnit
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class ScaWrapperControllerSpec extends AnyWordSpec with Matchers {

  // val wrapperDataRequest = fakeRequest.body.validate[WrapperDataRequest]
  private val fakeRequest = FakeRequest("POST", "/wrapper-data/:version").withHeaders("Content-Type" -> "application/json").withBody(
    Json.toJson(WrapperDataRequest(""))
  )

  lazy val appConfig : AppConfig = injector.instanceOf[AppConfig]

  private val controller = new ScaWrapperController(Helpers.stubControllerComponents(),appConfig)


  "test to check the version control" should {
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
      contentAsString(result).contains("Profile and settings") mustBe false
    }

    "test to check the error scenario" in {
      val version: String = "2.0.0"
      val result: Future[Result] = controller.wrapperData(version)(FakeRequest().withBody[JsValue](Json.parse("""{}""")))
      whenReady(result) { res =>
        res.header.status shouldBe 203
      }
    }
  }
}