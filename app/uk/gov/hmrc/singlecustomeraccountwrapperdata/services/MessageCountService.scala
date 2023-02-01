package uk.gov.hmrc.singlecustomeraccountwrapperdata.services

import com.google.inject.Inject
import play.api.Logging
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.singlecustomeraccountwrapperdata.connectors.ScaWrapperMessageConnector
import uk.gov.hmrc.singlecustomeraccountwrapperdata.models.MessageCount

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class MessageCountService @Inject() (
                                      servicesConfig: ServicesConfig,
                                      scaWrapperMessageConnector: ScaWrapperMessageConnector
                                    )(implicit executionContext: ExecutionContext)
  extends Logging {

  lazy val messageFrontendUrl: String = servicesConfig.baseUrl("message-frontend")


  def getUnreadMessageCount(implicit request: RequestHeader): Future[Option[Int]] =
    scaWrapperMessageConnector.getUnreadMessageCount.fold(
      _ => None,
      response => response.json.asOpt[MessageCount].map(_.count)
    ) recover { case _: Exception =>
      None
    }
}
