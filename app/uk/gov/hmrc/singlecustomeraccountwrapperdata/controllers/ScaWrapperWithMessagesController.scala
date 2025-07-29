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
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.*
import uk.gov.hmrc.singlecustomeraccountwrapperdata.connectors.MessageConnector
import uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers.actions.AuthAction

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

@Singleton()
class ScaWrapperWithMessagesController @Inject() (
  cc: ControllerComponents,
  val appConfig: AppConfig,
  val wrapperConfig: WrapperConfig,
  val urBannersConfig: UrBannersConfig,
  val webchatConfig: WebchatConfig,
  messageConnector: MessageConnector,
  authenticate: AuthAction
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with I18nSupport
    with Logging
    with WrapperDataBuilder {

  def wrapperDataWithMessages(lang: String, version: String): Action[AnyContent] = authenticate.async {
    implicit request =>
      implicit val playLang: Lang = Lang(lang)
      val wrapperData             = buildWrapperData(playLang, version)

      logger.info(s"[ScaWrapperWithMessagesController][wrapperDataWithMessages] Requesting unread message count")

      messageConnector.getUnreadMessageCount
        .map { maybeCount =>
          Ok(Json.toJson(wrapperData.copy(unreadMessageCount = maybeCount)))
        }
        .recover {
          case e: UpstreamErrorResponse if e.statusCode >= 400 && e.statusCode < 498 =>
            logger.error(
              s"[ScaWrapperWithMessagesController][wrapperDataWithMessages] Client error from upstream with status ${e.statusCode}",
              e
            )
            Ok(Json.toJson(wrapperData))

          case e: UpstreamErrorResponse if e.statusCode >= 499 =>
            logger.warn(
              s"[ScaWrapperWithMessagesController][wrapperDataWithMessages] Upstream server error with status ${e.statusCode}: ${e.message}"
            )
            Ok(Json.toJson(wrapperData))
          case NonFatal(ex)                                    =>
            logger.error(
              "[ScaWrapperWithMessagesController][wrapperDataWithMessages] Unexpected error when fetching unread message count",
              ex
            )
            Ok(Json.toJson(wrapperData))
        }
  }
}
