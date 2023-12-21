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
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.singlecustomeraccountwrapperdata.connectors.MessageConnector
import uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers.actions.AuthAction

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton()
class ScaMessageController @Inject()(cc: ControllerComponents, messageConnector: MessageConnector, authenticate: AuthAction)
                                    (implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def getUnreadMessageCount: Action[AnyContent] = authenticate.async { implicit request =>
    logger.info(s"[ScaMessageController][getUnreadMessageCount] Requesting unread message count")

    messageConnector.getUnreadMessageCount.map {
      case Some(unreadMessageCount) => Ok(Json.toJson(unreadMessageCount))
      case _ => NoContent
    }
  }

}
