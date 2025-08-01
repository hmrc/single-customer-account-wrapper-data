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
import play.api.i18n.{I18nSupport, Lang}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.*
import uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers.actions.AuthAction
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.WrapperDataResponse

import javax.inject.{Inject, Singleton}

@Singleton()
class ScaWrapperController @Inject() (
  cc: ControllerComponents,
  protected val appConfig: AppConfig,
  protected val wrapperConfig: WrapperConfig,
  protected val urBannersConfig: UrBannersConfig,
  protected val webchatConfig: WebchatConfig,
  authenticate: AuthAction
) extends BackendController(cc)
    with I18nSupport
    with Logging
    with WrapperDataBuilder {

  def wrapperData(lang: String, version: String): Action[AnyContent] = authenticate { implicit request =>
    implicit val playLang: Lang = Lang(lang)
    val wrapperData             = buildWrapperData(playLang, version)
    Ok(Json.toJson(wrapperData))
  }
}
