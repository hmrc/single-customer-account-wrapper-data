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

import scala.jdk.CollectionConverters.*

case class UrBannerDetails(
  titleEn: String,
  titleCy: String,
  linkTextEn: String,
  linkTextCy: String,
  hideCloseButton: Boolean
)

object UrBannerDetails {
  implicit val format: OFormat[UrBannerDetails] = Json.format[UrBannerDetails]
}

case class UrBanner(
  page: String,
  link: String,
  isEnabled: Boolean,
  bannerDetails: Option[UrBannerDetails] = None
)

object UrBanner {
  implicit val format: OFormat[UrBanner] = Json.format[UrBanner]
}

@Singleton
class UrBannersConfig @Inject() (configuration: Configuration) extends Logging {

  private val RootPath = "ur-banners.items"

  def getUrBannersByService: Map[String, List[UrBanner]] =
    if (!configuration.underlying.hasPath(RootPath)) {
      Map.empty
    } else {
      val configList = configuration.underlying.getConfigList(RootPath).asScala.toList

      configList.map { serviceConf =>
        val service = serviceConf.getString("service")

        val urBannerList: List[UrBanner] =
          serviceConf
            .getConfigList("entries")
            .asScala
            .toList
            .map { entryConf =>

              val hasAny =
                entryConf.hasPath("titleEn") ||
                  entryConf.hasPath("titleCy") ||
                  entryConf.hasPath("linkTextEn") ||
                  entryConf.hasPath("linkTextCy") ||
                  entryConf.hasPath("hideCloseButton")

              val bannerDetails =
                if (hasAny)
                  Some(
                    UrBannerDetails(
                      titleEn = entryConf.getString("titleEn"),
                      titleCy = entryConf.getString("titleCy"),
                      linkTextEn = entryConf.getString("linkTextEn"),
                      linkTextCy = entryConf.getString("linkTextCy"),
                      hideCloseButton = entryConf.getBoolean("hideCloseButton")
                    )
                  )
                else None

              UrBanner(
                page = entryConf.getString("page"),
                link = entryConf.getString("link"),
                isEnabled = entryConf.getBoolean("isEnabled"),
                bannerDetails = bannerDetails
              )
            }

        service -> urBannerList
      }.toMap
    }
}
