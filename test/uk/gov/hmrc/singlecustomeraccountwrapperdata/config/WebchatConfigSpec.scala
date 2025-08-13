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

package uk.gov.hmrc.singlecustomeraccountwrapperdata.config

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.singlecustomeraccountwrapperdata.fixtures.BaseSpec

class WebchatConfigSpec extends BaseSpec {

  lazy val testWebchat1: Webchat = Webchat("^/pattern1", "skin", true)
  lazy val testWebchat2: Webchat = Webchat("^/pattern2/sub.*", "Alternate", false)
  lazy val testWebchat3: Webchat = Webchat("^/pattern3.*", "skin", true)

  override implicit lazy val app: Application =
    GuiceApplicationBuilder()
      .configure(
        "webchat.items.0.service"               -> "test-frontend-1",
        "webchat.items.0.entries.0.pattern"     -> testWebchat1.pattern,
        "webchat.items.0.entries.0.skinElement" -> testWebchat1.skinElement,
        "webchat.items.0.entries.0.isEnabled"   -> testWebchat1.isEnabled,
        "webchat.items.0.entries.1.pattern"     -> testWebchat2.pattern,
        "webchat.items.0.entries.1.skinElement" -> testWebchat2.skinElement,
        "webchat.items.0.entries.1.isEnabled"   -> testWebchat2.isEnabled,
        "webchat.items.1.service"               -> "test-frontend-2",
        "webchat.items.1.entries.0.pattern"     -> testWebchat3.pattern,
        "webchat.items.1.entries.0.skinElement" -> testWebchat3.skinElement,
        "webchat.items.1.entries.0.isEnabled"   -> testWebchat3.isEnabled
      )
      .build()

  lazy val webchatConfig: WebchatConfig = app.injector.instanceOf[WebchatConfig]

  "WebchatConfig" must {
    "return a list of webchat pages for all services" in {
      webchatConfig.getWebchatUrlsByService mustBe
        Map(
          "test-frontend-1" -> List(testWebchat1, testWebchat2),
          "test-frontend-2" -> List(testWebchat3)
        )
    }

    "return empty map when no items are present" in {
      val appWithEmptyConfig = GuiceApplicationBuilder()
        .configure("webchat.items" -> List.empty)
        .build()

      val emptyConfig = appWithEmptyConfig.injector.instanceOf[WebchatConfig]
      emptyConfig.getWebchatUrlsByService mustBe Map.empty
    }
  }

  "Webchat" must {
    "serialize and deserialize correctly" in {
      val original = Webchat("/example-uri/.*", "popup", isEnabled = true)

      val json         = Json.toJson(original)
      val deserialized = json.as[Webchat]
      deserialized mustBe original
    }
  }
}
