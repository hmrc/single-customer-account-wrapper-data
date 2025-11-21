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
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.{AppConfig, BespokeUserResearchBanner, BespokeUserResearchBannerConfig, UrBanner, UrBannersConfig, WebchatConfig, WrapperConfig}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.WrapperDataResponse
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.auth.AuthenticatedRequest

trait WrapperDataBuilder extends Logging {

  protected val appConfig: AppConfig
  protected val wrapperConfig: WrapperConfig
  protected val urBannersConfig: UrBannersConfig
  protected val webchatConfig: WebchatConfig
  protected val bespokeUserResearchBannerConfig: BespokeUserResearchBannerConfig

  def buildWrapperData(lang: Lang, version: String)(implicit
    request: AuthenticatedRequest[AnyContent]
  ): WrapperDataResponse = {

    val wrapperDataVersion = appConfig.versionNum.take(1)
    val libraryVersion     = version.take(1)
    val isCompatible       = wrapperDataVersion == libraryVersion

    val userAgent = request.headers.get(HeaderNames.USER_AGENT)

    val urBannerList: List[UrBanner] =
      listIfCompatible(isCompatible, userAgent, urBannersConfig.getUrBannersByService)

    val webChatPages =
      listIfCompatible(isCompatible, userAgent, webchatConfig.getWebchatUrlsByService)

    val bespokeUserResearchBanner: Option[BespokeUserResearchBanner] =
      optionIfCompatible(
        compatible = isCompatible,
        userAgent = userAgent,
        serviceMap = bespokeUserResearchBannerConfig.getBespokeUserResearchBannersByService
      )

    if (isCompatible) {
      logger.info(s"[WrapperDataBuilder][buildWrapperData] Success - version: $wrapperDataVersion, lang: $lang")
      WrapperDataResponse(
        menuItemConfig = wrapperConfig.menuConfig()(request, lang),
        ptaMinMenuConfig = wrapperConfig.ptaMinMenuConfig(lang),
        urBanners = urBannerList,
        webchatPages = webChatPages,
        bespokeUserResearchBanner = bespokeUserResearchBanner,
        unreadMessageCount = None,
        trustedHelper = request.trustedHelper
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
        bespokeUserResearchBanner = None,
        unreadMessageCount = None,
        trustedHelper = request.trustedHelper
      )
    }
  }

  private def listForService[T](
    userAgent: Option[String],
    serviceMap: Map[String, List[T]]
  ): List[T] =
    userAgent.flatMap(serviceMap.get).getOrElse(Nil)

  private def listIfCompatible[T](
    compatible: Boolean,
    userAgent: Option[String],
    serviceMap: Map[String, List[T]]
  ): List[T] =
    if (compatible) listForService(userAgent, serviceMap) else Nil

  private def optionForService[T](
    userAgent: Option[String],
    serviceMap: Map[String, Option[T]]
  ): Option[T] =
    userAgent.flatMap(serviceMap.get).flatten

  private def optionIfCompatible[T](
    compatible: Boolean,
    userAgent: Option[String],
    serviceMap: Map[String, Option[T]]
  ): Option[T] =
    if (compatible) optionForService(userAgent, serviceMap) else None
}
