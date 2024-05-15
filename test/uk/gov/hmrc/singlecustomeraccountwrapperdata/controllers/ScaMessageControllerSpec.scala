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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.whenReady
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.singlecustomeraccountwrapperdata.connectors.MessageConnector

import scala.concurrent.Future

class ScaMessageControllerSpec extends AsyncWordSpec with Matchers with MockitoSugar with BeforeAndAfter {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val fakeRequest = FakeRequest("GET", "/")
  private val mockMessageConnector: MessageConnector = mock[MessageConnector]

  val modules: Seq[GuiceableModule] =
    Seq(
      inject.bind[MessageConnector].toInstance(mockMessageConnector)
    )

  val application: Application = new GuiceApplicationBuilder()
    .configure(conf = "auditing.enabled" -> false, "metrics.enabled" -> false, "metrics.jvm" -> false)
    .overrides(modules: _*)
    .build()

  private val controller = application.injector.instanceOf[ScaMessageController]

  before {
    reset(mockMessageConnector)
  }

  "GET /" must {
    "return unreadMessageCount if there is" in {

      val unreadMessageCount = 1
      when(mockMessageConnector.getUnreadMessageCount(any(), any()))
        .thenReturn(Future.successful(Some(unreadMessageCount)))

      val result = controller.getUnreadMessageCount(fakeRequest)

      whenReady(result) { _ =>
        status(result) mustBe OK
        contentAsJson(result) mustBe Json.parse(unreadMessageCount.toString)
      }

    }

    "return Not Found when user doesn't have any unread message" in {

      when(mockMessageConnector.getUnreadMessageCount(any(), any())).thenReturn(Future.successful(None))

      val result = controller.getUnreadMessageCount(fakeRequest)

      whenReady(result) { _ =>
        status(result) mustBe NO_CONTENT
      }

    }
  }
}
