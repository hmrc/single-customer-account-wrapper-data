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

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.MenuItemConfig

import scala.concurrent.duration.Duration

@Singleton
class AppConfig @Inject()(configuration: Configuration) {

  val appName: String = configuration.get[String]("appName")

  val pertaxUrl: String = s"${configuration.get[String]("sca-wrapper.internal.pertax-frontend.url")}/personal-account"
  val businessTaxAccountUrl: String = s"${configuration.get[String]("sca-wrapper.internal.business-tax-frontend.url")}/business-account"
  val feedbackFrontendUrl: String = s"${configuration.get[String]("sca-wrapper.internal.feedback-frontend.url")}/feedback"
  val contactUrl: String = s"${configuration.get[String]("sca-wrapper.internal.contact-frontend.url")}/contact/beta-feedback"
  val accessibilityStatementUrl: String = configuration.get[String]("sca-wrapper.internal.accessibility-statement-frontend.url")

  val defaultPertaxSignout = s"$pertaxUrl/signout/feedback/PERTAX"

  def menuConfig(signoutUrl: String): Seq[MenuItemConfig] = Seq(
    MenuItemConfig("Account Home", s"${pertaxUrl}", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None),
    MenuItemConfig("Messages", s"${pertaxUrl}/messages", leftAligned = false, position = 0, None, None),
    MenuItemConfig("Check progress", s"${pertaxUrl}/track", leftAligned = false, position = 1, None, None),
    MenuItemConfig("Profile and settings", s"${pertaxUrl}/profile-and-settings", leftAligned = false, position = 2, None, None),
    MenuItemConfig("Business tax account", s"${businessTaxAccountUrl}/business-account", leftAligned = false, position = 3, None, None),
    MenuItemConfig("Sign out", s"${signoutUrl}", leftAligned = false, position = 4, None, None)
  )

}
