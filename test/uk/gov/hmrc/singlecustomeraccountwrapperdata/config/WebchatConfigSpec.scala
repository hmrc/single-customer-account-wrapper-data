/*
 * Copyright 2024 HM Revenue & Customs
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
import uk.gov.hmrc.singlecustomeraccountwrapperdata.fixtures.BaseSpec

class WebchatConfigSpec extends BaseSpec {

  lazy val testWebchat1: Webchat = Webchat("/home", true)
  lazy val testWebchat2: Webchat = Webchat("/another-page", false)
  lazy val testWebchat3: Webchat = Webchat("/second-service", true)

  override implicit lazy val app: Application =
    GuiceApplicationBuilder()
      .configure(
        "webchat.0.service"     -> "test-frontend",
        "webchat.0.0.page"      -> testWebchat1.page,
        "webchat.0.0.isEnabled" -> testWebchat1.isEnabled,
        "webchat.0.1.page"      -> testWebchat2.page,
        "webchat.0.1.isEnabled" -> testWebchat2.isEnabled,
        "webchat.1.service"     -> "second-frontend",
        "webchat.1.0.page"      -> testWebchat3.page,
        "webchat.1.0.isEnabled" -> testWebchat3.isEnabled
      )
      .build()

  lazy val webchatConfig: WebchatConfig = app.injector.instanceOf[WebchatConfig]

  "WebchatConfig" must {
    "return a list of webchat pages for all services" in {
      webchatConfig.getWebchatUrlsByService must be
      Map(
        "test-frontend"   -> List(testWebchat1, testWebchat2),
        "second-frontend" -> List(testWebchat3)
      )
    }
  }
}
