/*
 * Copyright 2025 HM Revenue & Customs
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
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.singlecustomeraccountwrapperdata.config.AppConfig
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.MessageCountResponse

import scala.concurrent.{ExecutionContext, Future}

class MessageConnector @Inject() (httpClientV2: HttpClientV2, appConfig: AppConfig) extends Logging {

  def getUnreadMessageCount(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[Int]] = {
    val params = Seq("nino", "sautr", "HMRC-OBTDS-ORG", "HMRC-MTD-VAT", "HMRC-MTD-IT", "HMRC-PPT-ORG", "IR-PAYE")
      .map(t => s"taxIdentifiers=$t")
      .mkString("&")
    val url    = appConfig.messageServiceUrl + s"/secure-messaging/messages/count?$params"

    httpClientV2
      .get(url"$url")
      .execute[MessageCountResponse]
      .map { response =>
        val unreadCount = response.count.unread
        logger.info(
          s"[MessageConnector][getUnreadMessageCount] Unread message count requested, $unreadCount unread messages returned"
        )
        if (unreadCount > 0)
          Some(unreadCount)
        else None
      }
      .recover { case ex: Exception =>
        logger.error(s"[MessageConnector][getUnreadMessageCount] Exception: ${ex.getMessage}")
        None
      }
  }
}
