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

case class SmartAppBannerUrlConfigs(url: String, campaignId: String, iosArgs: String)

object SmartAppBannerUrlConfigs {
  implicit val format: OFormat[SmartAppBannerUrlConfigs] = Json.format[SmartAppBannerUrlConfigs]
}

@Singleton
class SmartAppBannerConfig @Inject() (configuration: Configuration) extends Logging {

  def getSmartAppBannersByService: Map[String, List[SmartAppBannerUrlConfigs]] = {

    val smartBannerConfig = configuration.get[Configuration]("smart-app-banner")

    val serviceMap = smartBannerConfig.subKeys.toList.map { key =>
      val serviceConfig = smartBannerConfig.get[Configuration](key)
      val serviceName = serviceConfig.get[String]("service")

      val urls = serviceConfig.get[Configuration]("urls").subKeys.toList.map { urlKey =>
        val urlConfig = serviceConfig.get[Configuration](s"urls.$urlKey")
        SmartAppBannerUrlConfigs(
          url = urlConfig.get[String]("url"),
          campaignId = urlConfig.get[String]("campaignId"),
          iosArgs = urlConfig.get[String]("iosArgs")
        )
      }

      serviceName -> urls
    }.toMap
    serviceMap
  }
}
