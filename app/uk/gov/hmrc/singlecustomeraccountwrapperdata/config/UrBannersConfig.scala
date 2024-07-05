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
    val config = configuration.underlying

    val maxItems: Int = configuration.get[Int]("ur-banners.max-items")
    val numberOfServices = (0 until maxItems).takeWhile(i => config.hasPathOrNull(s"ur-banners.$i")).size
    (0 until numberOfServices).map { indexService =>
      val service = configuration.get[String](s"ur-banners.$indexService.service")
      val numberOfPages = (0 until maxItems).takeWhile(j => config.hasPathOrNull(s"ur-banners.$indexService.$j")).size
      val urBanners: List[UrBanner] = (0 until numberOfPages).map { indexPage =>
        val page = configuration.get[String](s"ur-banners.$indexService.$indexPage.page")
        val link = configuration.get[String](s"ur-banners.$indexService.$indexPage.link")
        val isEnabled = configuration.get[Boolean](s"ur-banners.$indexService.$indexPage.isEnabled")
        UrBanner(page, link, isEnabled)
      }.toList

      service -> urBanners
    }.toMap
  }
}
