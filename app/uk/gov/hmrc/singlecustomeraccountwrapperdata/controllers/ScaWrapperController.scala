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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.{AppConfig, WrapperConfig}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers.actions.AuthAction
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.{WrapperDataRequest, WrapperDataResponse}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.auth.AuthenticatedRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class ScaWrapperController @Inject()(cc: ControllerComponents, appConfig: AppConfig, wrapperConfig: WrapperConfig,
                                     authenticate: AuthAction)(implicit ec: ExecutionContext) extends BackendController(cc) with I18nSupport {

  def wrapperData: Action[JsValue] = authenticate(parse.json).async { implicit request =>
    val wrapperDataVersion: String = appConfig.versionNum.take(1)
    val wrapperDataRequest = request.body.validate[WrapperDataRequest]
    wrapperDataRequest.fold(
      errors => {
        implicit val playLang: Lang = Lang("en")
        //TODO logging
        wrapperDataResponseVersionFallback(appConfig.defaultSignoutUrl)
      },
      req => {
        implicit val playLang: Lang = Lang(req.lang)
        if (wrapperDataVersion == req.wrapperLibraryVersion.take(1)) {
          wrapperDataResponse(req.signoutUrl)
        } else {
          wrapperDataResponseVersionFallback(req.signoutUrl)
        }
      }
    ).map(response => Ok(Json.toJson(response)))
  }

  private def wrapperDataResponse(signoutUrl: String)(implicit request: AuthenticatedRequest[JsValue], lang: Lang) = {
    wrapperConfig.menuConfig(signoutUrl).map { config =>
      WrapperDataResponse(
        config,
        wrapperConfig.ptaMinMenuConfig
      )
    }
  }

  private def wrapperDataResponseVersionFallback(signoutUrl: String)(implicit request: AuthenticatedRequest[JsValue], lang: Lang) = Future.successful {
    WrapperDataResponse(
      wrapperConfig.fallbackMenuConfig(signoutUrl),
      wrapperConfig.ptaMinMenuConfig
    )
  }

}
