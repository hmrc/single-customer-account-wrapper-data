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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.AppConfig
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.WrapperDataResponse

import javax.inject.{Inject, Singleton}

@Singleton()
class ScaWrapperController @Inject()(cc: ControllerComponents, appConfig: AppConfig) extends BackendController(cc) {

  def wrapperData(wrapperLibraryVersion: String): Action[AnyContent] = Action { implicit request =>
    val wrapperDataVersion : String = appConfig.versionNum.take(1)
    Ok(Json.toJson(
      if(wrapperDataVersion == wrapperLibraryVersion){
        Json.toJson(wrapperDataResponse)
      } else {
        Json.toJson(wrapperDataResponseFallback)
        }
    ))
  }

  private val wrapperDataResponse = {
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

  private val wrapperDataResponseFallback = {
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
}
