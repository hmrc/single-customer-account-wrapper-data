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

package uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers

import play.api.Logging
import play.api.http.HeaderNames
import play.api.i18n.Lang
import play.api.mvc.AnyContent
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.{AppConfig, UrBanner, UrBannersConfig, WebchatConfig, WrapperConfig}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.WrapperDataResponse
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.auth.AuthenticatedRequest

trait WrapperDataBuilder extends Logging {

  protected val appConfig: AppConfig
  protected val wrapperConfig: WrapperConfig
  protected val urBannersConfig: UrBannersConfig
  protected val webchatConfig: WebchatConfig

  def buildWrapperData(lang: Lang, version: String)(implicit
    request: AuthenticatedRequest[AnyContent]
  ): WrapperDataResponse = {
    val wrapperDataVersion: String   = appConfig.versionNum.take(1)
    val libraryVersion               = version.take(1)
    val httpUserAgent                = request.headers.get(HeaderNames.USER_AGENT)
    val urBannerList: List[UrBanner] =
      httpUserAgent.flatMap(urBannersConfig.getUrBannersByService.get).getOrElse(List.empty)
    val webChatPages                 = httpUserAgent.flatMap(webchatConfig.getWebchatUrlsByService.get).getOrElse(List.empty)

    if (wrapperDataVersion == libraryVersion) {
      logger.info(s"[WrapperDataBuilder][buildWrapperData] Success - version: $wrapperDataVersion, lang: $lang")
      WrapperDataResponse(
        menuItemConfig = wrapperConfig.menuConfig()(request, lang),
        ptaMinMenuConfig = wrapperConfig.ptaMinMenuConfig(lang),
        urBanners = urBannerList,
        webchatPages = webChatPages,
        unreadMessageCount = None
      )
    } else {
      logger.warn(
        s"[WrapperDataBuilder][buildWrapperData] Fallback - wrapper version: $wrapperDataVersion, library version: $libraryVersion, lang: $lang"
      )
      WrapperDataResponse(
        menuItemConfig = wrapperConfig.fallbackMenuConfig()(request, lang),
        ptaMinMenuConfig = wrapperConfig.ptaMinMenuConfig(lang),
        urBanners = List.empty,
        webchatPages = List.empty,
        unreadMessageCount = None
      )
    }
  }
}
