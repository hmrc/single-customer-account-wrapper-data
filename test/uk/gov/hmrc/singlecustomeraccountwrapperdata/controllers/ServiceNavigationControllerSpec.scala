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

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.singlecustomeraccountwrapperdata.fixtures.BaseSpec
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.ServiceNavigationToggleResponse

class ServiceNavigationControllerSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach with BaseSpec {

  private val controller = new ServiceNavigationController(
    cc = stubControllerComponents()
  )

  "ServiceNavigationController.toggle" must {

    "return useNewServiceNavigation = true" in {
      val result = controller.toggle()(FakeRequest())

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(
        ServiceNavigationToggleResponse(useNewServiceNavigation = true)
      )
    }
  }
}
