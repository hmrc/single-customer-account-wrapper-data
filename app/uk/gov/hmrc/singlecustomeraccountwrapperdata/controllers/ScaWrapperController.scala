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

import play.api.Logging
import play.api.http.HeaderNames
import play.api.i18n.{I18nSupport, Lang}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.{AppConfig, UrBanner, UrBannersConfig, WebchatConfig, WrapperConfig}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers.actions.AuthAction
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.WrapperDataResponse
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.auth.AuthenticatedRequest

import javax.inject.{Inject, Singleton}

@Singleton()
class ScaWrapperController @Inject() (
  cc: ControllerComponents,
  appConfig: AppConfig,
  wrapperConfig: WrapperConfig,
  urBannersConfig: UrBannersConfig,
  webchatConfig: WebchatConfig,
  authenticate: AuthAction
) extends BackendController(cc) with I18nSupport with Logging {

  def wrapperData(lang: String, version: String): Action[AnyContent] = authenticate { implicit request =>
    implicit val playLang: Lang = Lang(lang)

    val wrapperDataVersion: String = appConfig.versionNum.take(1)
    val libraryVersion = version.take(1)
    val httpUserAgent = request.headers.get(HeaderNames.USER_AGENT)
    val urBanners: List[UrBanner] =
      httpUserAgent.flatMap(urBannersConfig.getUrBannersByService.get(_)).getOrElse(List.empty)
    val webChatPages = httpUserAgent.flatMap(webchatConfig.getWebchatUrlsByService.get(_)).getOrElse(List.empty)

    val response = if (wrapperDataVersion == libraryVersion) {
      logger.info(
        s"[ScaWrapperController][wrapperData] Wrapper data successful request- version:$wrapperDataVersion, lang: $playLang"
      )
      WrapperDataResponse(wrapperConfig.menuConfig(), wrapperConfig.ptaMinMenuConfig, urBanners, webChatPages)
    } else {
      logger.warn(
        s"[ScaWrapperController][wrapperData] Wrapper data fallback request- version:$wrapperDataVersion, library version: $libraryVersion, lang: $playLang"
      )
      wrapperDataResponseVersionFallback()
    }
    Ok(Json.toJson(response))
  }

  private def wrapperDataResponseVersionFallback()(implicit request: AuthenticatedRequest[AnyContent], lang: Lang) =
    WrapperDataResponse(
      wrapperConfig.fallbackMenuConfig(),
      wrapperConfig.ptaMinMenuConfig,
      List.empty,
      List.empty
    )

}
