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

package uk.gov.hmrc.singlecustomeraccountwrapperdata.connectors

import com.google.inject.Inject
import play.api.Logging
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException}
import uk.gov.hmrc.play.partials.HtmlPartial._
import uk.gov.hmrc.play.partials.{HeaderCarrierForPartialsConverter, HtmlPartial}
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.AppConfig
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.MessageCountResponse

import scala.concurrent.{ExecutionContext, Future}

class MessageConnector @Inject()(http: HttpClient, appConfig: AppConfig) extends Logging {

  def getUnreadMessageCount(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[Int]] = {
    http.GET[MessageCountResponse](appConfig.messageUrl + "/messages?countOnly=true").map { count =>
      count.count.unread match {
        case num if num <= 0 => None
        case unreadCount => Some(unreadCount)
      }
    }.recoverWith {
      case ex: Exception =>
        println("error message count")
        Future.successful(None)
    }
  }
}
