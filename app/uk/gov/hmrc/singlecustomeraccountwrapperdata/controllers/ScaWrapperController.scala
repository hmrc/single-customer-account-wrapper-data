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

import play.api.i18n.{I18nSupport, Lang}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.AppConfig
import uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers.actions.AuthAction
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.WrapperDataResponse
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.auth.AuthenticatedRequest

import javax.inject.{Inject, Singleton}

@Singleton()
class ScaWrapperController @Inject()(cc: ControllerComponents, appConfig: AppConfig, authenticate: AuthAction) extends BackendController(cc) with I18nSupport {

  def wrapperData(wrapperLibraryVersion: String, lang: Option[String]): Action[AnyContent] = authenticate { implicit request =>
    val wrapperDataVersion: String = appConfig.versionNum.take(1)
    implicit val playLang: Lang = Lang(lang.getOrElse("en"))
    val response = (if (wrapperDataVersion == wrapperLibraryVersion.take(1)) {
      wrapperDataResponse
    } else {
      wrapperDataResponseVersionFallback
    })
    Ok(Json.toJson(response))
  }

  private def wrapperDataResponse(implicit request: AuthenticatedRequest[AnyContent], lang: Lang) = {
    WrapperDataResponse(
      appConfig.feedbackFrontendUrl,
      appConfig.contactUrl,
      appConfig.businessTaxAccountUrl,
      appConfig.pertaxUrl,
      appConfig.accessibilityStatementUrl,
      appConfig.ggSigninUrl,
      appConfig.menuConfig
    )
  }

  private def wrapperDataResponseVersionFallback(implicit request: AuthenticatedRequest[AnyContent], lang: Lang) = {
    WrapperDataResponse(
      appConfig.feedbackFrontendUrl,
      appConfig.contactUrl,
      appConfig.businessTaxAccountUrl,
      appConfig.pertaxUrl,
      appConfig.accessibilityStatementUrl,
      appConfig.ggSigninUrl,
      appConfig.fallbackMenuConfig
    )
  }

}
