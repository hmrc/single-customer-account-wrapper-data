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

  val versionNum: String = "1.0.0"

  final val pertaxUrl: String = s"${configuration.get[String]("sca-wrapper.pertax-frontend.url")}/personal-account"
  final val businessTaxAccountUrl: String = s"${configuration.get[String]("sca-wrapper.business-tax-frontend.url")}/business-account"
  final val feedbackFrontendUrl: String = s"${configuration.get[String]("sca-wrapper.feedback-frontend.url")}/feedback"
  final val contactUrl: String = s"${configuration.get[String]("sca-wrapper.contact-frontend.url")}/contact/beta-feedback"
  final val accessibilityStatementUrl: String = configuration.get[String]("sca-wrapper.accessibility-statement-frontend.url")
  final val ggSigninUrl: String = configuration.get[String]("sca-wrapper.gg.signin.url")

  final val defaultPertaxSignout = s"$pertaxUrl/signout/feedback/PERTAX"

//  final val messageFrontendUrl: String = configuration.get[String]("sca-wrapper.message-frontend.url")

  def menuConfig(implicit request: AuthenticatedRequest[AnyContent], lang: Lang): Seq[MenuItemConfig] = {
//    messageConnector.getUnreadMessageCount()
    btaConfig(
      Seq(
        MenuItemConfig(messages("menu.home"), s"${pertaxUrl}", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None),
        MenuItemConfig(messages("menu.messages"), s"${pertaxUrl}/messages", leftAligned = false, position = 0, None, None),
        MenuItemConfig(messages("menu.progress"), s"${pertaxUrl}/track", leftAligned = false, position = 1, None, None),
        MenuItemConfig(messages("menu.profile"), s"${pertaxUrl}/profile-and-settings", leftAligned = false, position = 2, None, None),
        MenuItemConfig(messages("menu.signout"), s"$defaultPertaxSignout", leftAligned = false, position = 4, None, None, signout = true)
      )
    )
  }

  def fallbackMenuConfig(implicit request: AuthenticatedRequest[AnyContent], lang: Lang): Seq[MenuItemConfig] = {
    btaConfig(
      Seq(
        MenuItemConfig(messages("menu.home"), s"${pertaxUrl}", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None),
        MenuItemConfig(messages("menu.messages"), s"${pertaxUrl}/messages", leftAligned = false, position = 1, None, None),
        MenuItemConfig(messages("menu.progress"), s"${pertaxUrl}/track", leftAligned = false, position = 1, None, None),
        MenuItemConfig(messages("menu.profile"), s"${pertaxUrl}/profile-and-settings", leftAligned = false, position = 2, None, None),
        MenuItemConfig(messages("menu.signout"), s"$defaultPertaxSignout", leftAligned = false, position = 4, None, None, signout = true)
      )
    )
  }
  private def btaConfig(config: Seq[MenuItemConfig])(implicit request: AuthenticatedRequest[AnyContent], lang: Lang) = {
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
