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

import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.AnyContent
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.auth.AuthenticatedRequest
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.{MenuItemConfig, PtaMinMenuConfig}

import javax.inject.{Inject, Singleton}

@Singleton
class WrapperConfig @Inject()(appConfig: AppConfig)(implicit val messages: MessagesApi) {

  def ptaMinMenuConfig(implicit lang: Lang): PtaMinMenuConfig = PtaMinMenuConfig(menuName = messages("menu.name"), backName = messages("menu.back"))

  def menuConfig()(implicit request: AuthenticatedRequest[AnyContent], lang: Lang): Seq[MenuItemConfig] = {
    btaConfig(
      Seq(
        MenuItemConfig("home", messages("menu.home"), s"${appConfig.pertaxUrl}", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None),
        MenuItemConfig("messages", messages("menu.messages"), s"${appConfig.pertaxUrl}/messages", leftAligned = false, position = 0, None, None),
        MenuItemConfig("progress", messages("menu.progress"), s"${appConfig.trackingHost}/track", leftAligned = false, position = 1, None, None),
        MenuItemConfig("profile", messages("menu.profile"), s"${appConfig.pertaxUrl}/profile-and-settings", leftAligned = false, position = 2, None, None),
        MenuItemConfig("signout", messages("menu.signout"), s"${appConfig.defaultSignoutUrl}", leftAligned = false, position = 4, None, None)
      )
    )
  }

  def fallbackMenuConfig()(implicit request: AuthenticatedRequest[AnyContent], lang: Lang): Seq[MenuItemConfig] = {
    btaConfig(
      Seq(
        MenuItemConfig("home", messages("menu.home"), s"${appConfig.pertaxUrl}", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None),
        MenuItemConfig("messages", messages("menu.messages"), s"${appConfig.pertaxUrl}/messages", leftAligned = false, position = 0, None, None),
        MenuItemConfig("progress", messages("menu.progress"), s"${appConfig.trackingHost}/track", leftAligned = false, position = 1, None, None),
        MenuItemConfig("profile", messages("menu.profile"), s"${appConfig.pertaxUrl}/profile-and-settings", leftAligned = false, position = 2, None, None),
        MenuItemConfig("signout", messages("menu.signout"), s"${appConfig.defaultSignoutUrl}", leftAligned = false, position = 4, None, None)
      )
    )
  }

  private def btaConfig(config: Seq[MenuItemConfig])(implicit request: AuthenticatedRequest[AnyContent], lang: Lang) = {
    val showBta = request.enrolments.find(_.key == "IR-SA").collectFirst {
      case Enrolment("IR-SA", Seq(identifier), "Activated", _) => identifier.value
    }.isDefined

    val btaConfig = Seq(MenuItemConfig("bta", messages("menu.bta"), s"${appConfig.businessTaxAccountUrl}", leftAligned = false, position = 3, None, None))
    if (showBta && request.trustedHelper.isEmpty) {
      config ++ btaConfig
    } else {
      config
    }
  }
}
