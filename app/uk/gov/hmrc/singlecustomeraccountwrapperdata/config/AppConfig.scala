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

import play.api.Configuration
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc.AnyContent
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.singlecustomeraccountwrapperdata.connectors.MessageConnector
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.MenuItemConfig
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.auth.AuthenticatedRequest

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(configuration: Configuration, messageConnector: MessageConnector)(implicit val messages: MessagesApi) {
//TODO split app config up into separate files

  final val appName: String = configuration.get[String]("appName")

  val versionNum: String = "1.0.2"

  private final val pertaxUrl: String = s"${configuration.get[String]("services.pertax-frontend.url")}/personal-account"
  private final val trackingUrl: String = s"${configuration.get[String]("services.tracking-frontend.url")}"
  private final val businessTaxAccountUrl: String = s"${configuration.get[String]("services.business-tax-frontend.url")}/business-account"
//  final val messageFrontendUrl: String = configuration.get[String]("services.message-frontend.url")
  final val defaultSignoutUrl: String = configuration.get[String]("services.gg-signout.url")

  def menuConfig(signoutUrl: String)(implicit request: AuthenticatedRequest[JsValue], lang: Lang): Seq[MenuItemConfig] = {
//    messageConnector.getUnreadMessageCount()
    btaConfig(
      Seq(
        MenuItemConfig(messages("menu.home"), s"${pertaxUrl}", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None),
        MenuItemConfig(messages("menu.messages"), s"${pertaxUrl}/messages", leftAligned = false, position = 0, None, None),
        MenuItemConfig(messages("menu.progress"), s"${trackingUrl}/track", leftAligned = false, position = 1, None, None),
        MenuItemConfig(messages("menu.profile"), s"${pertaxUrl}/profile-and-settings", leftAligned = false, position = 2, None, None),
        MenuItemConfig(messages("menu.signout"), s"$signoutUrl", leftAligned = false, position = 4, None, None, signout = true)
      )
    )
  }

  def fallbackMenuConfig(signoutUrl: String)(implicit request: AuthenticatedRequest[JsValue], lang: Lang): Seq[MenuItemConfig] = {
    btaConfig(
      Seq(
        MenuItemConfig(messages("menu.home"), s"${pertaxUrl}", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None),
        MenuItemConfig(messages("menu.messages"), s"${pertaxUrl}/messages", leftAligned = false, position = 1, None, None),
        MenuItemConfig(messages("menu.progress"), s"${trackingUrl}/track", leftAligned = false, position = 1, None, None),
        MenuItemConfig(messages("menu.profile"), s"${pertaxUrl}/profile-and-settings", leftAligned = false, position = 2, None, None),
        MenuItemConfig(messages("menu.signout"), s"$signoutUrl", leftAligned = false, position = 4, None, None, signout = true)
      )
    )
  }
  private def btaConfig(config: Seq[MenuItemConfig])(implicit request: AuthenticatedRequest[JsValue], lang: Lang) = {
    val showBta = request.enrolments.find(_.key == "IR-SA").collectFirst {
      case Enrolment("IR-SA", Seq(identifier), "Activated", _) => identifier.value
    }.isDefined

    val btaConfig = Seq(MenuItemConfig(messages("menu.bta"), s"${businessTaxAccountUrl}", leftAligned = false, position = 3, None, None))
    if(showBta) {
      config ++ btaConfig
    } else {
      config
    }
  }
}
