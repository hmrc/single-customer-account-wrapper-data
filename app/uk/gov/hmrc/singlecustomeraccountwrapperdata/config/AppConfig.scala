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

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(configuration: Configuration) {

  final val appName: String = configuration.get[String]("appName")

  val versionNum: String = "1.0.3"

  final val pertaxUrl: String = s"${configuration.get[String]("services.pertax-frontend.url")}/personal-account"
  final val trackingUrl: String = s"${configuration.get[String]("services.tracking-frontend.url")}"
  final val businessTaxAccountUrl: String = s"${configuration.get[String]("services.business-tax-frontend.url")}/business-account"
  final val messageUrl: String = configuration.get[String]("services.message.url")
  final val defaultSignoutUrl: String = configuration.get[String]("services.gg-signout.url")
}
