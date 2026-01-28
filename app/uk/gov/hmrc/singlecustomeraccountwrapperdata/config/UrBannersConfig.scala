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
import scala.util.{Failure, Success, Try}

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

              val detailKeys  = List("titleEn", "titleCy", "linkTextEn", "linkTextCy", "hideCloseButton")
              val presentKeys = detailKeys.filter(entryConf.hasPath)

              val bannerDetails: Option[UrBannerDetails] =
                presentKeys match {
                  case Nil =>
                    None

                  case keys if keys.size == detailKeys.size =>
                    Try {
                      UrBannerDetails(
                        titleEn = entryConf.getString("titleEn"),
                        titleCy = entryConf.getString("titleCy"),
                        linkTextEn = entryConf.getString("linkTextEn"),
                        linkTextCy = entryConf.getString("linkTextCy"),
                        hideCloseButton = entryConf.getBoolean("hideCloseButton")
                      )
                    } match {
                      case Success(details) =>
                        Some(details)

                      case Failure(e) =>
                        logger.warn(
                          s"[UrBannersConfig] Invalid ur-banners entry for service='$service', page='${entryConf
                              .getString("page")}'. " +
                            "All bespoke fields were present but could not be parsed; defaulting bannerDetails=None.",
                          e
                        )
                        None
                    }

                  case keys =>
                    val missing = detailKeys.diff(keys)
                    logger.warn(
                      s"[UrBannersConfig] Invalid ur-banners entry for service='$service', page='${entryConf.getString("page")}'. " +
                        s"Bespoke fields must be all-or-nothing. Present: ${keys.mkString(", ")}. Missing: ${missing
                            .mkString(", ")}. " +
                        "Defaulting bannerDetails=None."
                    )
                    None
                }

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
