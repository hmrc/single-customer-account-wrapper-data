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

package uk.gov.hmrc.singlecustomeraccountwrapperdata.models

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.{UrBanner, Webchat}

case class WrapperDataResponse(
  menuItemConfig: scala.collection.Seq[MenuItemConfig],
  ptaMinMenuConfig: PtaMinMenuConfig,
  urBanners: List[UrBanner],
  webchatPages: List[Webchat]
)

object WrapperDataResponse {
  implicit val format: OFormat[WrapperDataResponse] = Json.format[WrapperDataResponse]
}
