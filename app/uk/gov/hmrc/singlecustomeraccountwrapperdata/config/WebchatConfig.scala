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

package uk.gov.hmrc.singlecustomeraccountwrapperdata.config

import com.google.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OFormat}
import play.api.{Configuration, Logging}

import scala.jdk.CollectionConverters._

case class Webchat(pattern: String, skinElement: String, isEnabled: Boolean, chatType: String)

object Webchat {
  implicit val format: OFormat[Webchat] = Json.format[Webchat]
}

@Singleton
class WebchatConfig @Inject() (configuration: Configuration) extends Logging {

  def getWebchatUrlsByService: Map[String, List[Webchat]] = {
    val configList = configuration.underlying.getConfigList("webchat.items").asScala.toList

    configList.map { serviceConf =>
      val service                    = serviceConf.getString("service")
      val webChatList: List[Webchat] = serviceConf.getConfigList("entries").asScala.toList.map { entryConf =>
        Webchat(
          pattern = entryConf.getString("pattern"),
          skinElement = entryConf.getString("skinElement"),
          isEnabled = entryConf.getBoolean("isEnabled"),
          chatType = entryConf.getString("chatType")
        )
      }
      service -> webChatList
    }.toMap
  }
}
