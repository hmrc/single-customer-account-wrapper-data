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

package uk.gov.hmrc.singlecustomeraccountwrapperdata.controllers.actions

import com.google.inject.{ImplementedBy, Inject}
import play.api.Logging
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Name, ~}
import uk.gov.hmrc.domain
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.auth.AuthenticatedRequest

import scala.concurrent.{ExecutionContext, Future}

class AuthActionImpl @Inject()(
                                override val authConnector: AuthConnector,
                                val parser: BodyParsers.Default)
                              (implicit val executionContext: ExecutionContext) extends AuthorisedFunctions with AuthAction with Logging {

  object GTOE200 {
    def unapply(confLevel: ConfidenceLevel): Option[ConfidenceLevel] =
      if (confLevel.level >= ConfidenceLevel.L200.level) Some(confLevel) else None
  }

  object LT200 {
    def unapply(confLevel: ConfidenceLevel): Option[ConfidenceLevel] =
      if (confLevel.level < ConfidenceLevel.L200.level) Some(confLevel) else None
  }

  def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)//session!

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
      case nino ~ _ ~ Enrolments(enrolments) ~ Some(credentials) ~ Some(CredentialStrength.strong) ~
        GTOE200(confidenceLevel) ~ name ~ trustedHelper ~ profile =>
        val trimmedRequest: Request[A] = request
          .map {
            case AnyContentAsFormUrlEncoded(data) =>
              AnyContentAsFormUrlEncoded(data.map { case (key, vals) =>
                (key, vals.map(_.trim))
              })
            case b => b
          }
          .asInstanceOf[Request[A]]

        val authenticatedRequest = AuthenticatedRequest[A](
          trustedHelper.fold(nino.map(domain.Nino))(helper => Some(domain.Nino(helper.principalNino))),
          credentials,
          confidenceLevel,
          Some(trustedHelper.fold(name.getOrElse(Name(None, None)))(helper => Name(Some(helper.principalName), None))),
          trustedHelper,
          None,
          enrolments,
          trimmedRequest
        )
        block(authenticatedRequest)
      case nino ~ _ ~ Enrolments(enrolments) ~ Some(credentials) ~ Some(credentialStrength) ~
        LT200(confidenceLevel) ~ name ~ trustedHelper ~ profile =>
        val trimmedRequest: Request[A] = request
          .map {
            case AnyContentAsFormUrlEncoded(data) =>
              AnyContentAsFormUrlEncoded(data.map { case (key, vals) =>
                (key, vals.map(_.trim))
              })
            case b => b
          }
          .asInstanceOf[Request[A]]

        val unauthenticatedRequest = AuthenticatedRequest[A](
          trustedHelper.fold(nino.map(domain.Nino))(helper => Some(domain.Nino(helper.principalNino))),
          credentials,
          confidenceLevel,
          Some(trustedHelper.fold(name.getOrElse(Name(None, None)))(helper => Name(Some(helper.principalName), None))),
          trustedHelper,
          None,
          enrolments,
          trimmedRequest
        )
        block(unauthenticatedRequest)
    }
  }
//    .recover {
//    case authException =>
//      logger.error(authException.getMessage)
//      Redirect(
//        appConfig.loginUrl,
//        Map("continue" -> Seq(appConfig.loginContinueUrl), "origin" -> Seq("single-customer-account-frontend")))
//  }
}
//Set(Enrolment("IR-SA", Seq(EnrolmentIdentifier("UTR", "1632631936")), "Activated", None)),

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction
  extends ActionBuilder[AuthenticatedRequest, AnyContent] with ActionFunction[Request, AuthenticatedRequest] {
}
