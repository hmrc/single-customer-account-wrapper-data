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
import uk.gov.hmrc.singlecustomeraccountwrapperdata.fixtures.BaseSpec

class BespokeUserResearchBannerConfigSpec extends BaseSpec {

  lazy val enabledBanner1: BespokeUserResearchBanner = BespokeUserResearchBanner(
    url = "https://service-1.example.com/ur-1",
    titleEn = "Help us improve service 1",
    titleCy = "Helpwch ni i wella gwasanaeth 1",
    linkTextEn = "Take the survey 1",
    linkTextCy = "Cymerwch yr arolwg 1",
    hideCloseButton = false
  )

  lazy val enabledBanner2First: BespokeUserResearchBanner = BespokeUserResearchBanner(
    url = "https://service-2.example.com/ur-1",
    titleEn = "First enabled banner",
    titleCy = "Baner gyntaf wedi&#39;i galluogi",
    linkTextEn = "First survey",
    linkTextCy = "Arolwg cyntaf",
    hideCloseButton = true
  )

  lazy val enabledBanner2Second: BespokeUserResearchBanner = BespokeUserResearchBanner(
    url = "https://service-2.example.com/ur-2",
    titleEn = "Second enabled banner",
    titleCy = "Ail faner wedi&#39;i galluogi",
    linkTextEn = "Second survey",
    linkTextCy = "Ail arolwg"
  )

  lazy val disabledBanner: BespokeUserResearchBanner = BespokeUserResearchBanner(
    url = "https://service-3.example.com/ur-disabled",
    titleEn = "Disabled banner",
    titleCy = "Baner wedi&#39;i hanalluogi",
    linkTextEn = "Disabled survey",
    linkTextCy = "Arolwg wedi&#39;i analluogi"
  )

  override implicit lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        "bespoke-ur-banners.items.0.service"                   -> "test-frontend-1",
        "bespoke-ur-banners.items.0.entries.0.url"             -> enabledBanner1.url,
        "bespoke-ur-banners.items.0.entries.0.titleEn"         -> enabledBanner1.titleEn,
        "bespoke-ur-banners.items.0.entries.0.titleCy"         -> enabledBanner1.titleCy,
        "bespoke-ur-banners.items.0.entries.0.linkTextEn"      -> enabledBanner1.linkTextEn,
        "bespoke-ur-banners.items.0.entries.0.linkTextCy"      -> enabledBanner1.linkTextCy,
        "bespoke-ur-banners.items.0.entries.0.hideCloseButton" -> enabledBanner1.hideCloseButton,
        "bespoke-ur-banners.items.0.entries.0.isEnabled"       -> true,
        "bespoke-ur-banners.items.0.entries.1.url"             -> "https://service-1.example.com/ignored",
        "bespoke-ur-banners.items.0.entries.1.titleEn"         -> "Ignored",
        "bespoke-ur-banners.items.0.entries.1.titleCy"         -> "Anwybyddwyd",
        "bespoke-ur-banners.items.0.entries.1.linkTextEn"      -> "Ignored link",
        "bespoke-ur-banners.items.0.entries.1.linkTextCy"      -> "Dolen anwybyddwyd",
        "bespoke-ur-banners.items.0.entries.1.hideCloseButton" -> false,
        "bespoke-ur-banners.items.0.entries.1.isEnabled"       -> false,
        "bespoke-ur-banners.items.1.service"                   -> "test-frontend-2",
        "bespoke-ur-banners.items.1.entries.0.url"             -> enabledBanner2First.url,
        "bespoke-ur-banners.items.1.entries.0.titleEn"         -> enabledBanner2First.titleEn,
        "bespoke-ur-banners.items.1.entries.0.titleCy"         -> enabledBanner2First.titleCy,
        "bespoke-ur-banners.items.1.entries.0.linkTextEn"      -> enabledBanner2First.linkTextEn,
        "bespoke-ur-banners.items.1.entries.0.linkTextCy"      -> enabledBanner2First.linkTextCy,
        "bespoke-ur-banners.items.1.entries.0.hideCloseButton" -> enabledBanner2First.hideCloseButton,
        "bespoke-ur-banners.items.1.entries.0.isEnabled"       -> true,
        "bespoke-ur-banners.items.1.entries.1.url"             -> enabledBanner2Second.url,
        "bespoke-ur-banners.items.1.entries.1.titleEn"         -> enabledBanner2Second.titleEn,
        "bespoke-ur-banners.items.1.entries.1.titleCy"         -> enabledBanner2Second.titleCy,
        "bespoke-ur-banners.items.1.entries.1.linkTextEn"      -> enabledBanner2Second.linkTextEn,
        "bespoke-ur-banners.items.1.entries.1.linkTextCy"      -> enabledBanner2Second.linkTextCy,
        "bespoke-ur-banners.items.1.entries.1.hideCloseButton" -> enabledBanner2Second.hideCloseButton,
        "bespoke-ur-banners.items.1.entries.1.isEnabled"       -> true,
        "bespoke-ur-banners.items.2.service"                   -> "test-frontend-3",
        "bespoke-ur-banners.items.2.entries.0.url"             -> disabledBanner.url,
        "bespoke-ur-banners.items.2.entries.0.titleEn"         -> disabledBanner.titleEn,
        "bespoke-ur-banners.items.2.entries.0.titleCy"         -> disabledBanner.titleCy,
        "bespoke-ur-banners.items.2.entries.0.linkTextEn"      -> disabledBanner.linkTextEn,
        "bespoke-ur-banners.items.2.entries.0.linkTextCy"      -> disabledBanner.linkTextCy,
        "bespoke-ur-banners.items.2.entries.0.hideCloseButton" -> disabledBanner.hideCloseButton,
        "bespoke-ur-banners.items.2.entries.0.isEnabled"       -> false
      )
      .build()

  lazy val bespokeConfig: BespokeUserResearchBannerConfig =
    app.injector.instanceOf[BespokeUserResearchBannerConfig]

  "BespokeUserResearchBannerConfig" must {

    "return an option of the first enabled bespoke banner per service" in {
      bespokeConfig.getBespokeUserResearchBannersByService mustBe Map(
        "test-frontend-1" -> Some(enabledBanner1),
        "test-frontend-2" -> Some(enabledBanner2First),
        "test-frontend-3" -> None
      )
    }

    "return an empty map when the root config path does not exist" in {
      val appWithNoConfig = new GuiceApplicationBuilder()
        .build()

      val cfg = appWithNoConfig.injector.instanceOf[BespokeUserResearchBannerConfig]

      cfg.getBespokeUserResearchBannersByService mustBe Map.empty
    }

    "return an empty map when items list is present but empty" in {
      val appWithEmptyItems = new GuiceApplicationBuilder()
        .configure("bespoke-ur-banners.items" -> List.empty)
        .build()

      val cfg = appWithEmptyItems.injector.instanceOf[BespokeUserResearchBannerConfig]

      cfg.getBespokeUserResearchBannersByService mustBe Map.empty
    }
  }
}
