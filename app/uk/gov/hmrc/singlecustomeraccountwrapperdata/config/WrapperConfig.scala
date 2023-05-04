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
import play.api.libs.json.JsValue
import play.api.mvc.AnyContent
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.singlecustomeraccountwrapperdata.connectors.MessageConnector
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.auth.AuthenticatedRequest
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.{MenuItemConfig, PtaMinMenuConfig}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WrapperConfig @Inject()(messageConnector: MessageConnector, appConfig: AppConfig)(implicit val messages: MessagesApi, ec: ExecutionContext) {

  def ptaMinMenuConfig(implicit lang: Lang): PtaMinMenuConfig = PtaMinMenuConfig(menuName = messages("menu.name"), backName = messages("menu.back"))

  def menuConfig()(implicit request: AuthenticatedRequest[AnyContent], lang: Lang, hc: HeaderCarrier): Future[Seq[MenuItemConfig]] = {
    messageConnector.getUnreadMessageCount.map { count =>
      btaConfig(
        Seq(
          MenuItemConfig(messages("menu.home"), s"${appConfig.pertaxUrl}", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None),
          MenuItemConfig(messages("menu.messages"), s"${appConfig.pertaxUrl}/messages", leftAligned = false, position = 0, None, count),
          MenuItemConfig(messages("menu.progress"), s"${appConfig.trackingUrl}/track", leftAligned = false, position = 1, None, None),
          MenuItemConfig(messages("menu.profile"), s"${appConfig.pertaxUrl}/your-profile", leftAligned = false, position = 2, None, None),
          MenuItemConfig(messages("menu.signout"), s"${appConfig.defaultSignoutUrl}", leftAligned = false, position = 4, None, None, signout = true)
        )
      )
    }
  }

  def fallbackMenuConfig()(implicit request: AuthenticatedRequest[AnyContent], lang: Lang): Seq[MenuItemConfig] = {
    btaConfig(
      Seq(
        MenuItemConfig(messages("menu.home"), s"${appConfig.pertaxUrl}", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None),
        MenuItemConfig(messages("menu.messages"), s"${appConfig.pertaxUrl}/messages", leftAligned = false, position = 0, None, None),
        MenuItemConfig(messages("menu.progress"), s"${appConfig.trackingUrl}/track", leftAligned = false, position = 1, None, None),
        MenuItemConfig(messages("menu.profile"), s"${appConfig.pertaxUrl}/your-profile", leftAligned = false, position = 2, None, None),
        MenuItemConfig(messages("menu.signout"), s"${appConfig.defaultSignoutUrl}", leftAligned = false, position = 4, None, None, signout = true)
      )
    )
  }

  private def btaConfig(config: Seq[MenuItemConfig])(implicit request: AuthenticatedRequest[AnyContent], lang: Lang) = {
    val showBta = request.enrolments.find(_.key == "IR-SA").collectFirst {
      case Enrolment("IR-SA", Seq(identifier), "Activated", _) => identifier.value
    }.isDefined

    val btaConfig = Seq(MenuItemConfig(messages("menu.bta"), s"${appConfig.businessTaxAccountUrl}", leftAligned = false, position = 3, None, None))
    if (showBta) {
      config ++ btaConfig
    } else {
      config
    }
  }
}
