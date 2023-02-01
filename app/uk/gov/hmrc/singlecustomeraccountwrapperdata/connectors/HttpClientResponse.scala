package uk.gov.hmrc.singlecustomeraccountwrapperdata.connectors

import cats.data.EitherT
import com.google.inject.Inject
import play.api.Logging
import play.api.http.Status.{BAD_GATEWAY, LOCKED, NOT_FOUND, TOO_MANY_REQUESTS}
import uk.gov.hmrc.http.{HttpException, HttpResponse, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}

class HttpClientResponse @Inject() (implicit ec: ExecutionContext) extends Logging {

  def read(
            response: Future[Either[UpstreamErrorResponse, HttpResponse]]
          ): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    EitherT(response.map {
      case Right(response)                                                                 =>
        Right(response)
      case Left(error) if error.statusCode == NOT_FOUND                                    =>
        logger.info(error.message)
        Left(error)
      case Left(error) if error.statusCode == LOCKED                                       =>
        logger.warn(error.message)
        Left(error)
      case Left(error) if error.statusCode >= 499 || error.statusCode == TOO_MANY_REQUESTS =>
        logger.error(error.message)
        Left(error)
      case Left(error)                                                                     =>
        logger.error(error.message, error)
        Left(error)
    } recover {
      case exception: HttpException =>
        logger.error(exception.message)
        Left(UpstreamErrorResponse(exception.message, BAD_GATEWAY, BAD_GATEWAY))
      case exception: Exception     =>
        throw exception
    })
}
