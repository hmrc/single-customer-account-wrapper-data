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

package uk.gov.hmrc.singlecustomeraccountwrapperdata.services

import com.google.inject.Inject
import play.api.Logging
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Name, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.AuthResponse

import scala.concurrent.{ExecutionContext, Future}

class AuthRetrieval @Inject()(
                               override val authConnector: AuthConnector)
                             (implicit val executionContext: ExecutionContext, hc: HeaderCarrier) extends AuthorisedFunctions with Logging {

  object GTOE200 {
    def unapply(confLevel: ConfidenceLevel): Option[ConfidenceLevel] =
      if (confLevel.level >= ConfidenceLevel.L200.level) Some(confLevel) else None
  }

  def retrieveAuth = {
    authorised().retrieve(
      Retrievals.nino and
        Retrievals.affinityGroup and
        Retrievals.allEnrolments and
        Retrievals.credentials and
        Retrievals.credentialStrength and
        Retrievals.confidenceLevel and
        Retrievals.name and
        Retrievals.trustedHelper and
        Retrievals.profile
    ) {
      case nino ~ _ ~ Enrolments(enrolments) ~ Some(credentials) ~ Some(credentialStrength) ~
        GTOE200(confidenceLevel) ~ name ~ trustedHelper ~ profile =>

        val authResponse = AuthResponse(
          nino = nino,
          enrolments = enrolments,
          confidenceLevel = confidenceLevel,
          name = Some(trustedHelper.fold(name.getOrElse(Name(None, None)))(helper => Name(Some(helper.principalName), None))),
          trustedHelper = trustedHelper,
          credentials = credentials,
          credentialStrength = credentialStrength
        )
        Future.successful(authResponse)
    }
  }
}
