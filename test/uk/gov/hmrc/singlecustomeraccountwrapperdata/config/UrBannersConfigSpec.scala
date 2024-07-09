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

class UrBannersConfigSpec extends BaseSpec {

  lazy val testBanner1: UrBanner = UrBanner("/home", "TestLink1")
  lazy val testBanner2: UrBanner = UrBanner("/another-page", "TestLink2")
  lazy val testBanner3: UrBanner = UrBanner("/second-service", "TestLink3")

  override implicit lazy val app: Application =
    GuiceApplicationBuilder()
      .configure(
        "ur-banners.0.service" -> "test-frontend",
        "ur-banners.0.0.page"  -> testBanner1.page,
        "ur-banners.0.0.link"  -> testBanner1.link,
        "ur-banners.0.1.page"  -> testBanner2.page,
        "ur-banners.0.1.link"  -> testBanner2.link,
        "ur-banners.1.service" -> "second-frontend",
        "ur-banners.1.0.page"  -> testBanner3.page,
        "ur-banners.1.0.link"  -> testBanner3.link
      )
      .build()

  lazy val bannersConfig: UrBannersConfig = app.injector.instanceOf[UrBannersConfig]

  "UrBannersConfig" must {
    "return a list of banners for all services" in {
      bannersConfig.getUrBannersByService must be
      Map(
        "test-frontend"   -> List(testBanner1, testBanner2),
        "second-frontend" -> List(testBanner3)
      )
    }
  }
}
