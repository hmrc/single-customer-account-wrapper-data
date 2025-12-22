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
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.{ScaServiceNavigationToggle, ServiceNavigationToggleResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

@Singleton
class ServiceNavigationController @Inject() (
  cc: ControllerComponents,
  featureFlagService: FeatureFlagService
)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def toggle(): Action[AnyContent] = Action.async { implicit request =>
    featureFlagService
      .get(ScaServiceNavigationToggle)
      .map { toggle =>
        Ok(Json.toJson(ServiceNavigationToggleResponse(useNewServiceNavigation = toggle.isEnabled)))
      }
      .recover { case NonFatal(e) =>
        logger.error("[ServiceNavigationController][toggle] failed to read feature flag", e)
        Ok(Json.toJson(ServiceNavigationToggleResponse(useNewServiceNavigation = false)))
      }
  }
}
