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

case class UrBanner(page: String, link: String, isEnabled: Boolean)

object UrBanner {
  implicit val format: OFormat[UrBanner] = Json.format[UrBanner]
}

/*

Map(
  service -> List[UrBanner]
)

 */

@Singleton
class UrBannersConfig @Inject() (configuration: Configuration) extends Logging {

  def getUrBannersByService: Map[String, List[UrBanner]] = {
    val configList = configuration.underlying.getConfigList("ur-banners.items").asScala.toList
    configList.map { serviceConf =>
      val service                      = serviceConf.getString("service")
      val urBannerList: List[UrBanner] = serviceConf.getConfigList("entries").asScala.toList.map { entryConf =>
        UrBanner(
          page = entryConf.getString("page"),
          link = entryConf.getString("link"),
          isEnabled = entryConf.getBoolean("isEnabled")
        )
      }
      service -> urBannerList
    }.toMap
  }
}
