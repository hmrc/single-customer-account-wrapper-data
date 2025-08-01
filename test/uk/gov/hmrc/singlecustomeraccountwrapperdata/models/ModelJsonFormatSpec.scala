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

package uk.gov.hmrc.singlecustomeraccountwrapperdata.models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.{UrBanner, Webchat}

class ModelJsonFormatSpec extends AnyWordSpec with Matchers {

  private val menuItemConfig = MenuItemConfig(
    id = "menu",
    text = "Profile",
    href = "http://localhost:9232/personal-account/messages",
    leftAligned = true,
    position = 0,
    icon = Some("hmrc-account-icon hmrc-account-icon--home"),
    notificationBadge = Some(3)
  )

  private val ptaMinMenuConfig = PtaMinMenuConfig(menuName = "Account menu", backName = "Back")

  "MenuItemConfig" must {
    "serialize and deserialize correctly" in {
      val json         = Json.toJson(menuItemConfig)
      val deserialized = json.as[MenuItemConfig]
      deserialized mustBe menuItemConfig
    }
  }

  "MessageCount" must {
    "serialize and deserialize correctly" in {
      val original     = MessageCount(total = 10, unread = 3)
      val json         = Json.toJson(original)
      val deserialized = json.as[MessageCount]
      deserialized mustBe original
    }
  }

  "MessageCountResponse" must {
    "serialize and deserialize correctly" in {
      val messageCount = MessageCount(total = 10, unread = 3)
      val original     = MessageCountResponse(messageCount)
      val json         = Json.toJson(original)
      val deserialized = json.as[MessageCountResponse]
      deserialized mustBe original
    }
  }

  "PtaMinMenuConfig" must {
    "serialize and deserialize correctly" in {
      val json         = Json.toJson(ptaMinMenuConfig)
      val deserialized = json.as[PtaMinMenuConfig]
      deserialized mustBe ptaMinMenuConfig
    }
  }

  "WrapperDataResponse" must {
    "serialize and deserialize correctly with unreadMessageCount None" in {
      val original     = WrapperDataResponse(
        menuItemConfig = Seq(menuItemConfig),
        ptaMinMenuConfig = ptaMinMenuConfig,
        urBanners = List(UrBanner("/example", "https://link1.example.com", isEnabled = true)),
        webchatPages = List(Webchat("/example-uri/.*", "popup", isEnabled = true)),
        unreadMessageCount = None
      )
      val json         = Json.toJson(original)
      val deserialized = json.as[WrapperDataResponse]
      deserialized mustBe original
    }

    "serialize and deserialize correctly with unreadMessageCount Some" in {
      val original     = WrapperDataResponse(
        menuItemConfig = Seq(menuItemConfig),
        ptaMinMenuConfig = ptaMinMenuConfig,
        urBanners = List(UrBanner("/example", "https://link1.example.com", isEnabled = true)),
        webchatPages = List(Webchat("/example-uri/.*", "popup", isEnabled = true)),
        unreadMessageCount = Some(5)
      )
      val json         = Json.toJson(original)
      val deserialized = json.as[WrapperDataResponse]
      deserialized mustBe original
    }
  }
}
