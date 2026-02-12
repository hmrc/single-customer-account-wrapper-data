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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.mongoFeatureToggles.model.FeatureFlag
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService
import uk.gov.hmrc.singlecustomeraccountwrapperdata.fixtures.BaseSpec
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.{ScaServiceNavigationToggle, ServiceNavigationToggleResponse}

import scala.concurrent.Future

class ServiceNavigationControllerSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach with BaseSpec {

  private val mockFeatureFlagService: FeatureFlagService = mock[FeatureFlagService]

  private val controller = new ServiceNavigationController(
    cc = stubControllerComponents(),
    featureFlagService = mockFeatureFlagService
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockFeatureFlagService)
  }

  "ServiceNavigationController.toggle" must {

    "return useNewServiceNavigation = true when the feature flag is enabled" in {
      val enabledFlag = FeatureFlag(ScaServiceNavigationToggle, isEnabled = true)

      when(mockFeatureFlagService.get(any()))
        .thenReturn(Future.successful(enabledFlag))

      val result = controller.toggle()(FakeRequest())

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(
        ServiceNavigationToggleResponse(useNewServiceNavigation = true)
      )
    }

    "return useNewServiceNavigation = false when the feature flag is disabled" in {
      val disabledFlag = FeatureFlag(ScaServiceNavigationToggle, isEnabled = false)

      when(mockFeatureFlagService.get(any()))
        .thenReturn(Future.successful(disabledFlag))

      val result = controller.toggle()(FakeRequest())

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(
        ServiceNavigationToggleResponse(useNewServiceNavigation = false)
      )
    }

    "return useNewServiceNavigation = false when the feature flag service fails" in {
      when(mockFeatureFlagService.get(any()))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val result = controller.toggle()(FakeRequest())

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(
        ServiceNavigationToggleResponse(useNewServiceNavigation = false)
      )
    }
  }
}
