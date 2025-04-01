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

package uk.gov.hmrc.singlecustomeraccountwrapperdata.config

import com.google.inject.{Inject, Singleton}
import play.api.libs.json.{Json, OFormat}
import play.api.{Configuration, Logging}

case class Webchat(pattern: String, skinElement: String, isEnabled: Boolean)

object Webchat {
  implicit val format: OFormat[Webchat] = Json.format[Webchat]
}

@Singleton
class WebchatConfig @Inject() (configuration: Configuration) extends Logging {

  def getWebchatUrlsByService: Map[String, List[Webchat]] = {
    val config = configuration.underlying

    val maxItems: Int = configuration.get[Int]("webchat.max-items")
    val numberOfServices = (0 until maxItems).takeWhile(i => config.hasPathOrNull(s"webchat.$i")).size
    (0 until numberOfServices).map { indexService =>
      val service = configuration.get[String](s"webchat.$indexService.service")
      val numberOfPages = (0 until maxItems).takeWhile(j => config.hasPathOrNull(s"webchat.$indexService.$j")).size
      val webchatUrls: List[Webchat] = (0 until numberOfPages).map { indexPage =>
        val pattern = configuration.get[String](s"webchat.$indexService.$indexPage.pattern")
        val skinElement = configuration.get[String](s"webchat.$indexService.$indexPage.skinElement")
        val isEnabled = configuration.get[Boolean](s"webchat.$indexService.$indexPage.isEnabled")
        Webchat(pattern, skinElement, isEnabled)
      }.toList
      service -> webchatUrls
    }.toMap
  }
}
