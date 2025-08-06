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

class UrBannersConfigSpec extends BaseSpec {

  lazy val testBanner1: UrBanner = UrBanner("/home", "TestLink1", true)
  lazy val testBanner2: UrBanner = UrBanner("/first-page", "TestLink2", false)
  lazy val testBanner3: UrBanner = UrBanner("/second-page", "TestLink3", true)

  override implicit lazy val app: Application =
    GuiceApplicationBuilder()
      .configure(
        "ur-banners.items.0.service"             -> "test-frontend-1",
        "ur-banners.items.0.entries.0.page"      -> testBanner1.page,
        "ur-banners.items.0.entries.0.link"      -> testBanner1.link,
        "ur-banners.items.0.entries.0.isEnabled" -> testBanner1.isEnabled,
        "ur-banners.items.0.entries.1.page"      -> testBanner2.page,
        "ur-banners.items.0.entries.1.link"      -> testBanner2.link,
        "ur-banners.items.0.entries.1.isEnabled" -> testBanner2.isEnabled,
        "ur-banners.items.1.service"             -> "test-frontend-2",
        "ur-banners.items.1.entries.0.page"      -> testBanner3.page,
        "ur-banners.items.1.entries.0.link"      -> testBanner3.link,
        "ur-banners.items.1.entries.0.isEnabled" -> testBanner3.isEnabled
      )
      .build()

  lazy val bannersConfig: UrBannersConfig = app.injector.instanceOf[UrBannersConfig]

  "UrBannersConfig" must {
    "return a list of banners for all services" in {
      bannersConfig.getUrBannersByService mustBe
        Map(
          "test-frontend-1" -> List(testBanner1, testBanner2),
          "test-frontend-2" -> List(testBanner3)
        )
    }

    "return empty map when no items are present" in {
      val appWithEmptyConfig = GuiceApplicationBuilder()
        .configure("ur-banners.items" -> List.empty)
        .build()

      val emptyConfig = appWithEmptyConfig.injector.instanceOf[UrBannersConfig]
      emptyConfig.getUrBannersByService mustBe Map.empty
    }
  }

  "UrBanner" must {
    "serialize and deserialize correctly" in {
      val original = UrBanner("/example", "https://link1.example.com", isEnabled = true)

      val json         = Json.toJson(original)
      val deserialized = json.as[UrBanner]
      deserialized mustBe original
    }
  }
}
