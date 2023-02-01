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

import akka.stream.TLSRole.server
import org.mockito.ArgumentMatchers.any
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import play.api.mvc.{Action, AnyContent, Result}
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers.{GET, contentAsString, defaultAwaitTimeout, redirectLocation, route, writeableOf_AnyContentAsEmpty, status => httpStatus}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.AppConfig
import uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers.actions.AuthAction
import uk.gov.hmrc.singlecustomeraccountwrapperdata.fixtures.SpecBase


import scala.concurrent.{ExecutionContext, Future}

class ScaWrapperControllerSpec extends SpecBase {


  lazy val appConfig: AppConfig = injector.instanceOf[AppConfig]
  lazy val authAction: AuthAction = injector.instanceOf[AuthAction]

  private val controller = new ScaWrapperController(Helpers.stubControllerComponents(), appConfig, authAction)

  // val wrapperDataRequest = fakeRequest.body.validate[WrapperDataRequest]
  override lazy val fakeRequest = FakeRequest("GET", "/")



  /*"test to check the version control" must {
    "test to check the menu configuration with same versions" in {
      val mockMicroserviceAuthConnector = mock[AuthConnector]

      when(mockMicroserviceAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(Future.successful(()))
      val version: String = "1.0.0"
      val result = controller.wrapperData(version)(fakeRequest)
      println(result)
      whenReady(result) { res =>
        res.header.status shouldBe 200
      }
      contentAsString(result).contains("Profile and settings") mustBe true
    }

    "test to check the menu configuration with different versions" in {
      val version: String = "2.0.0"
      val result: Future[Result] = controller.wrapperData(version)(fakeRequest)
      whenReady(result) { res =>
        res.header.status shouldBe 200
      }
      contentAsString(result).contains("Profile and settings") mustBe false
    }


  }*/
}