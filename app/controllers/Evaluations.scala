/*
 * Happy Melly Teller
 * Copyright (C) 2013 - 2014, Happy Melly http://www.happymelly.com
 *
 * This file is part of the Happy Melly Teller.
 *
 * Happy Melly Teller is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Happy Melly Teller is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Happy Melly Teller.  If not, see <http://www.gnu.org/licenses/>.
 *
 * If you have questions concerning this license or the applicable additional terms, you may contact
 * by email Sergey Kotlov, sergey.kotlov@happymelly.com or
 * in writing Happy Melly One, Handelsplein 37, Rotterdam, The Netherlands, 3071 PR
 */

package controllers

import models._
import play.api.mvc._
import org.joda.time._
import play.api.data._
import play.api.data.validation.Constraints._
import play.api.data.Forms._
import models.UserRole.Role._
import play.api.data.format.Formatter
import play.api.i18n.Messages
import java.io.File
import play.api.cache.Cache
import services._
import play.api.data.FormError
import scala.Some
import securesocial.core.SecuredRequest
import fly.play.s3.{ BucketFile, S3Exception }
import play.api.Play.current
import scala.io.Source
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import services.EmailService
import play.api.i18n.Messages

object Evaluations extends Controller with Security {

  /**
   * Formatter used to define a form mapping for the `EvaluationStatus` enumeration.
   */
  implicit def statusFormat: Formatter[EvaluationStatus.Value] = new Formatter[EvaluationStatus.Value] {

    def bind(key: String, data: Map[String, String]) = {
      try {
        data.get(key).map(EvaluationStatus.withName(_)).toRight(Seq.empty)
      } catch {
        case e: NoSuchElementException ⇒ Left(Seq(FormError(key, "error.invalid")))
      }
    }

    def unbind(key: String, value: EvaluationStatus.Value) = Map(key -> value.toString)
  }

  val statusMapping = of[EvaluationStatus.Value]

  /** HTML form mapping for creating and editing. */
  def evaluationForm(implicit request: SecuredRequest[_]) = Form(mapping(
    "id" -> ignored(Option.empty[Long]),
    "eventId" -> longNumber.verifying(
      "Such event doesn't exist", (eventId: Long) ⇒ Event.find(eventId).isDefined),
    "participantId" -> optional(longNumber),
    "question1" -> nonEmptyText,
    "question2" -> nonEmptyText,
    "question3" -> nonEmptyText,
    "question4" -> nonEmptyText,
    "question5" -> nonEmptyText,
    "question6" -> nonEmptyText.transform(_.toInt, (l: Int) ⇒ l.toString),
    "question7" -> nonEmptyText.transform(_.toInt, (l: Int) ⇒ l.toString),
    "question8" -> nonEmptyText,
    "status" -> statusMapping,
    "handled" -> optional(jodaLocalDate),
    "certificate" -> optional(nonEmptyText),
    "created" -> ignored(DateTime.now),
    "createdBy" -> ignored(request.user.fullName),
    "updated" -> ignored(DateTime.now),
    "updatedBy" -> ignored(request.user.fullName))(Evaluation.apply)(Evaluation.unapply))

  /** Add page **/
  def add = SecuredDynamicAction("evaluation", "add") { implicit request ⇒
    implicit handler ⇒
      generateCertificate()

      val account = request.user.asInstanceOf[LoginIdentity].userAccount
      val events = findEvents(account)
      Ok(views.html.evaluation.form(request.user, None, evaluationForm, events))
  }

  /** Add form submits to this action **/
  def create = SecuredDynamicAction("evaluation", "add") { implicit request ⇒
    implicit handler ⇒

      val form: Form[Evaluation] = evaluationForm.bindFromRequest
      form.fold(
        formWithErrors ⇒ {
          val account = request.user.asInstanceOf[LoginIdentity].userAccount
          val events = findEvents(account)
          BadRequest(views.html.evaluation.form(request.user, None, formWithErrors, events))
        },
        evaluation ⇒ {
          val createdEvaluation = evaluation.insert

          val brand = Brand.find(createdEvaluation.event.brandCode).get
          val recipients = brand.coordinator :: createdEvaluation.event.facilitators
          val impression = Messages("evaluation.impression." + createdEvaluation.question6)
          val subject = s"New evaluation (General impression: ${impression})"
          EmailService.send(recipients.toSet, subject,
            mail.html.evaluation(createdEvaluation).toString, true)

          val activity = Activity.insert(request.user.fullName, Activity.Predicate.Created, "new evaluation")
          Redirect(routes.Participants.index()).flashing("success" -> activity.toString)
        })
  }

  /** Delete an evaluation **/
  def delete(id: Long) = SecuredDynamicAction("evaluation", "manage") { implicit request ⇒
    implicit handler ⇒

      Evaluation.find(id).map {
        evaluation ⇒
          evaluation.delete
          val activity = Activity.insert(request.user.fullName, Activity.Predicate.Deleted, "evaluation")
          Redirect(routes.Participants.index).flashing("success" -> activity.toString)
      }.getOrElse(NotFound)
  }

  /** Details page **/
  def details(id: Long) = SecuredRestrictedAction(Viewer) { implicit request ⇒
    implicit handler ⇒

      Evaluation.find(id).map {
        evaluation ⇒
          Ok(views.html.evaluation.details(request.user, evaluation))
      }.getOrElse(NotFound)

  }

  /** Edit page **/
  def edit(id: Long) = SecuredDynamicAction("evaluation", "edit") { implicit request ⇒
    implicit handler ⇒

      Evaluation.find(id).map { evaluation ⇒
        val account = request.user.asInstanceOf[LoginIdentity].userAccount
        val events = findEvents(account)
        Ok(views.html.evaluation.form(request.user, Some(evaluation), evaluationForm.fill(evaluation), events))
      }.getOrElse(NotFound)

  }

  /** Edit form submits to this action **/
  def update(id: Long) = SecuredDynamicAction("evaluation", "edit") { implicit request ⇒
    implicit handler ⇒

      Evaluation.find(id).map { existingEvaluation ⇒
        val form: Form[Evaluation] = evaluationForm.bindFromRequest
        form.fold(
          formWithErrors ⇒ {
            val account = request.user.asInstanceOf[LoginIdentity].userAccount
            val events = findEvents(account)
            BadRequest(views.html.evaluation.form(request.user, Some(existingEvaluation), form, events))
          },
          evaluation ⇒ {
            evaluation.copy(id = Some(id)).update
            val activity = Activity.insert(request.user.fullName, Activity.Predicate.Updated, "evaluation")
            Redirect(routes.Participants.index).flashing("success" -> activity.toString)
          })
      }.getOrElse(NotFound)
  }

  /** Approve form submits to this action **/
  def approve(id: Long) = SecuredDynamicAction("evaluation", "manage") { implicit request ⇒
    implicit handler ⇒
      Evaluation.find(id).map { existingEvaluation ⇒
        existingEvaluation.approve
        val activity = Activity.insert(request.user.fullName, Activity.Predicate.Approved,
          existingEvaluation.participant.fullName)

        val facilitator = request.user.asInstanceOf[LoginIdentity].userAccount.person.get
        val brand = Brand.find(existingEvaluation.event.brandCode).get
        val participant = existingEvaluation.participant
        val recipients = participant :: brand.coordinator :: existingEvaluation.event.facilitators
        val subject = s"Your ${brand.brand.name} certificate"
        EmailService.send(recipients.toSet, subject,
          mail.html.approved(brand.brand, participant, facilitator).toString, true)

        Redirect(routes.Participants.index).flashing("success" -> activity.toString)
      }.getOrElse(NotFound)
  }

  /** Reject form submits to this action **/
  def reject(id: Long) = SecuredDynamicAction("evaluation", "manage") { implicit request ⇒
    implicit handler ⇒
      Evaluation.find(id).map { existingEvaluation ⇒
        existingEvaluation.reject
        val activity = Activity.insert(request.user.fullName, Activity.Predicate.Rejected,
          existingEvaluation.participant.fullName)

        val facilitator = request.user.asInstanceOf[LoginIdentity].userAccount.person.get
        val brand = Brand.find(existingEvaluation.event.brandCode).get
        val participant = existingEvaluation.participant
        val recipients = participant :: brand.coordinator :: existingEvaluation.event.facilitators
        val subject = s"Your ${brand.brand.name} certificate"
        EmailService.send(recipients.toSet, subject,
          mail.html.rejected(brand.brand, participant, facilitator).toString, true)

        Redirect(routes.Participants.index).flashing("success" -> activity.toString)
      }.getOrElse(NotFound)
  }

  private def generateCertificate() {
    import com.itextpdf.text.Document
    import com.itextpdf.text.pdf.PdfWriter
    import com.itextpdf.text.pdf.PdfReader
    import com.itextpdf.text.DocumentException;
    import com.itextpdf.text.Paragraph;
    import com.itextpdf.tool.xml.XMLWorkerHelper;

    import java.io.File;
    import java.io.FileInputStream;
    import java.io.FileOutputStream;
    import java.io.FileNotFoundException;

    import play.Logger

    val document = new Document();
    // step 2
    val writer = PdfWriter.getInstance(document, new FileOutputStream("test.pdf"));
    // step 3
    document.open();
    // step 4
    XMLWorkerHelper.getInstance().parseXHtml(writer, document,
      new FileInputStream("test.html"));
    // step 5
    document.close();
  }

  private def findEvents(account: UserAccount): List[Event] = {
    if (account.editor) {
      Event.findAll
    } else {
      Event.findByCoordinator(account.personId)
    }
  }
}
