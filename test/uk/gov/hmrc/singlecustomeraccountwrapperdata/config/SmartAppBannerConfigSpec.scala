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

class SmartAppBannerConfigSpec extends BaseSpec {

  lazy val testSmartAppBanner1: SmartAppBannerUrlConfigs = SmartAppBannerUrlConfigs("/home", "campaign1", "iosArgs1")
  lazy val testSmartAppBanner2: SmartAppBannerUrlConfigs =
    SmartAppBannerUrlConfigs("/another-page", "campaign2", "iosArgs2")
  lazy val testSmartAppBanner3: SmartAppBannerUrlConfigs =
    SmartAppBannerUrlConfigs("/second-service", "campaign3", "iosArgs3")

  override implicit lazy val app: Application =
    GuiceApplicationBuilder()
      .configure(
        "smart-app-banner.0.service"           -> "test-one-frontend",
        "smart-app-banner.0.urls.0.url"        -> testSmartAppBanner1.url,
        "smart-app-banner.0.urls.1.url"        -> testSmartAppBanner2.url,
        "smart-app-banner.0.urls.0.iosArgs"    -> testSmartAppBanner1.iosArgs,
        "smart-app-banner.0.urls.1.iosArgs"    -> testSmartAppBanner2.iosArgs,
        "smart-app-banner.0.urls.0.campaignId" -> testSmartAppBanner1.campaignId,
        "smart-app-banner.0.urls.1.campaignId" -> testSmartAppBanner2.campaignId,
        "smart-app-banner.1.service"           -> "test-two-frontend",
        "smart-app-banner.1.urls.0.url"        -> testSmartAppBanner3.url,
        "smart-app-banner.1.urls.0.iosArgs"    -> testSmartAppBanner3.iosArgs,
        "smart-app-banner.1.urls.0.campaignId" -> testSmartAppBanner3.campaignId
      )
      .build()

  lazy val smartAppBannerConfig: SmartAppBannerConfig = app.injector.instanceOf[SmartAppBannerConfig]

  "UrBannersConfig" must {
    "return a list of banners for all services" in {
      smartAppBannerConfig.getSmartAppBannersByService must be
      Map(
        "test-one-frontend" -> List(testSmartAppBanner1, testSmartAppBanner2),
        "test-two-frontend" -> List(testSmartAppBanner3)
      )
    }
  }
}
