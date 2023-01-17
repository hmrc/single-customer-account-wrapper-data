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
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.MenuItemConfig

@Singleton
class AppConfig @Inject()(configuration: Configuration) {

  val appName: String = configuration.get[String]("appName")

  private val pertaxBaseUrl: String = configuration.get[String]("sca-wrapper.pertax-frontend.base-url")
  private val pertaxServicePath: String = configuration.get[String]("sca-wrapper.pertax-frontend.service-path")
  private val pertaxSignoutPath: String = configuration.get[String]("sca-wrapper.pertax-frontend.signout-path")
  val pertaxUrl = s"$pertaxBaseUrl/$pertaxServicePath"
  private val usePertaxSignout: Boolean = configuration.get[Boolean]("sca-wrapper.signout.use-pertax-signout")
  private val continueUrl: Option[String] = configuration.getOptional[String]("sca-wrapper.signout.continue-url")
  private val origin: Option[String] = configuration.getOptional[String]("sca-wrapper.signout.origin")
  val pertaxSignoutUrl = s"$pertaxUrl/$pertaxSignoutPath"
  val customSignoutUrl: Option[String] = configuration.get[Option[String]]("sca-wrapper.signout.custom-signout-url")

  private val businessTaxAccountBaseUrl: String = configuration.get[String]("sca-wrapper.business-tax-frontend.base-url")
  val businessTaxAccountServicePath: String = configuration.get[String]("sca-wrapper.business-tax-frontend.service-path")
  val businessTaxAccountUrl: String = s"$businessTaxAccountBaseUrl/$businessTaxAccountServicePath"


  private def signoutUrl: String = {
    if (usePertaxSignout) {
      pertaxSignoutUrl
    } else {
      //TODO url validation
      customSignoutUrl match {
        case Some(url) if url.nonEmpty => customSignoutUrl.get
        case _ => pertaxSignoutUrl
      }
    }
  }
  private def signoutParams(continueUrl: Option[String], origin: Option[String]) = {
    val contUrl = s"${continueUrl.fold("") { url => s"continueUrl=$url" }}"
    val originUrl = s"${origin.fold("") { url => s"origin=$url" }}"
    (contUrl, originUrl) match {
      case _ if contUrl.nonEmpty && origin.nonEmpty => s"?$contUrl&$originUrl"
      case _ if contUrl.isEmpty && origin.isEmpty => ""
      case x@_ => s"?${x._1}${x._2}"
    }
  }

  //wrapper data handle all links
  val menuConfig: Seq[MenuItemConfig] = Seq(
    MenuItemConfig("Account Home", s"${pertaxUrl}", leftAligned = true, position = 0, Some("hmrc-account-icon hmrc-account-icon--home"), None),
    MenuItemConfig("Messages", s"${pertaxUrl}/messages", leftAligned = false, position = 0, None, None),
    MenuItemConfig("Check progress", s"${pertaxUrl}/track", leftAligned = false, position = 1, None, None),
    MenuItemConfig("Profile and settings", s"${pertaxUrl}/profile-and-settings", leftAligned = false, position = 2, None, None),
    MenuItemConfig("Business tax account", s"${businessTaxAccountUrl}/business-account", leftAligned = false, position = 3, None, None),
    MenuItemConfig("Sign out", s"${signoutUrl}${signoutParams(continueUrl, origin)}", leftAligned = false, position = 4, None, None)
  )

}
