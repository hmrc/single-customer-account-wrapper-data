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
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(servicesConfig: ServicesConfig, configuration: Configuration) {

  val versionNum: String = "1.0.3"

  // ms-url for backend call
  val messageServiceUrl: String = servicesConfig.baseUrl(serviceName = "message")

  // fe-host to redirect on browser
  private lazy val pertaxHost: String = getExternalUrl("pertax-frontend.host").getOrElse("")
  lazy val trackingHost: String = getExternalUrl("tracking-frontend.host").getOrElse("")
  private lazy val businessTaxAccountHost: String = getExternalUrl("business-tax-frontend.host").getOrElse("")
  private lazy val caHost: String = getExternalUrl("ca-frontend.host").getOrElse("")

  val pertaxUrl: String = s"$pertaxHost/personal-account"
  val businessTaxAccountUrl: String = s"$businessTaxAccountHost/business-account"
  val defaultSignoutUrl: String = s"$caHost/gg/sign-out"

  private def getExternalUrl(key: String): Option[String] =
    configuration.getOptional[String](s"external-url.$key")
}
