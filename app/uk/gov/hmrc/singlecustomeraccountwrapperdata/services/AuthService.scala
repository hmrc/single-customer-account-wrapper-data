package uk.gov.hmrc.singlecustomeraccountwrapperdata.services

import com.google.inject.Inject
import uk.gov.hmrc.auth.core.Enrolment

import scala.concurrent.{ExecutionContext, Future}

class AuthService @Inject()(authRetrieval: AuthRetrieval)(implicit val executionContext: ExecutionContext){

  def showBtaLink: Future[Boolean] = {
    authRetrieval.retrieveAuth.map { auth =>
      auth.enrolments.find(_.key == "IR-SA").collectFirst {
        case Enrolment("IR-SA", Seq(identifier), "Activated", _) => identifier.value
      }.isDefined
    }
  }
}
