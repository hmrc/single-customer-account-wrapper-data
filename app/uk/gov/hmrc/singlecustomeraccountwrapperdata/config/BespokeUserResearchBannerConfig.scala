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
import play.api.{Configuration, Logging}

import scala.jdk.CollectionConverters._

@Singleton
class BespokeUserResearchBannerConfig @Inject() (configuration: Configuration) extends Logging {

  private val RootPath = "bespoke-ur-banners.items"

  def getBespokeUserResearchBannersByService: Map[String, Option[BespokeUserResearchBanner]] =
    if (!configuration.underlying.hasPath(RootPath)) {
      Map.empty
    } else {
      val services = configuration.underlying.getConfigList(RootPath).asScala.toList

      services.map { serviceConf =>
        val service = serviceConf.getString("service")

        val enabledBanner: Option[BespokeUserResearchBanner] =
          serviceConf
            .getConfigList("entries")
            .asScala
            .toList
            .flatMap { entry =>
              val banner =
                if entry.getBoolean("isEnabled") then
                  Some(
                    BespokeUserResearchBanner(
                      url = entry.getString("url"),
                      titleEn = entry.getString("titleEn"),
                      titleCy = entry.getString("titleCy"),
                      linkTextEn = entry.getString("linkTextEn"),
                      linkTextCy = entry.getString("linkTextCy"),
                      hideCloseButton = entry.getBoolean("hideCloseButton")
                    )
                  )
                else None
              banner
            }
            .headOption

        service -> enabledBanner
      }.toMap
    }
}
