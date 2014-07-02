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

import java.io.{ File, FileOutputStream }
import models._
import models.UserRole.Role._
import org.joda.time._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.FormError
import play.api.Play.current
import play.api.i18n.Messages
import securesocial.core.SecuredRequest
import services.EmailService

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
  def add(eventId: Option[Long], participantId: Option[Long]) = SecuredDynamicAction("evaluation", "add") {
    implicit request ⇒
      implicit handler ⇒
        val account = request.user.asInstanceOf[LoginIdentity].userAccount
        val events = findEvents(account)
        Ok(views.html.evaluation.form(request.user, None, evaluationForm, events, eventId, participantId))
  }

  /** Add form submits to this action **/
  def create = SecuredDynamicAction("evaluation", "add") { implicit request ⇒
    implicit handler ⇒

      val form: Form[Evaluation] = evaluationForm.bindFromRequest
      form.fold(
        formWithErrors ⇒ {
          val account = request.user.asInstanceOf[LoginIdentity].userAccount
          val events = findEvents(account)
          BadRequest(views.html.evaluation.form(request.user, None, formWithErrors, events, None, None))
        },
        evaluation ⇒ {
          val createdEvaluation = evaluation.insert

          val brand = Brand.find(createdEvaluation.event.brandCode).get
          val recipients = brand.coordinator :: createdEvaluation.event.facilitators
          val impression = Messages("evaluation.impression." + createdEvaluation.question6)
          val subject = s"New evaluation (General impression: ${impression})"
          EmailService.send(createdEvaluation.event.facilitators.toSet,
            Some(Set(brand.coordinator)), None, subject,
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
          evaluation.delete()
          val activity = Activity.insert(request.user.fullName, Activity.Predicate.Deleted, "evaluation")
          Redirect(routes.Participants.index()).flashing("success" -> activity.toString)
      }.getOrElse(NotFound)
  }

  /** Details page **/
  def details(id: Long) = SecuredRestrictedAction(Viewer) { implicit request ⇒
    implicit handler ⇒

      Evaluation.find(id).map {
        evaluation ⇒
          {
            val brand = Brand.find(evaluation.event.brandCode).get
            Ok(views.html.evaluation.details(request.user, evaluation, brand.brand))
          }
      }.getOrElse(NotFound)

  }

  /** Edit page **/
  def edit(id: Long) = SecuredDynamicAction("evaluation", "edit") { implicit request ⇒
    implicit handler ⇒

      Evaluation.find(id).map { evaluation ⇒
        val account = request.user.asInstanceOf[LoginIdentity].userAccount
        val events = findEvents(account)
        Ok(views.html.evaluation.form(request.user, Some(evaluation), evaluationForm.fill(evaluation), events, None, None))
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
            BadRequest(views.html.evaluation.form(request.user, Some(existingEvaluation), form, events, None, None))
          },
          evaluation ⇒ {
            evaluation.copy(id = Some(id)).update
            val activity = Activity.insert(request.user.fullName, Activity.Predicate.Updated, "evaluation")
            Redirect(routes.Participants.index()).flashing("success" -> activity.toString)
          })
      }.getOrElse(NotFound)
  }

  /** Approve form submits to this action **/
  def approve(id: Long) = SecuredDynamicAction("evaluation", "manage") { implicit request ⇒
    implicit handler ⇒
      Evaluation.find(id).map { ev ⇒

        val approver = request.user.asInstanceOf[LoginIdentity].userAccount.person.get
        ev.approve(approver)

        val activity = Activity.insert(request.user.fullName, Activity.Predicate.Approved,
          ev.participant.fullName)
        Redirect(routes.Participants.index()).flashing("success" -> activity.toString)
      }.getOrElse(NotFound)
  }

  /** Reject form submits to this action **/
  def reject(id: Long) = SecuredDynamicAction("evaluation", "manage") { implicit request ⇒
    implicit handler ⇒
      Evaluation.find(id).map { existingEvaluation ⇒
        existingEvaluation.reject()
        val activity = Activity.insert(request.user.fullName, Activity.Predicate.Rejected,
          existingEvaluation.participant.fullName)

        val facilitator = request.user.asInstanceOf[LoginIdentity].userAccount.person.get
        val brand = Brand.find(existingEvaluation.event.brandCode).get
        val participant = existingEvaluation.participant
        val subject = s"Your ${brand.brand.name} certificate"
        EmailService.send(Set(participant),
          Some(existingEvaluation.event.facilitators.toSet),
          Some(Set(brand.coordinator)), subject,
          mail.html.rejected(brand.brand, participant, facilitator).toString(), richMessage = true)

        Redirect(routes.Participants.index()).flashing("success" -> activity.toString)
      }.getOrElse(NotFound)
  }

  /**
   * Generate a XLSX report with evaluations
   *
   * @param brandCode filter events by a brand
   * @param eventId a selected event
   * @param status  filter events by their statuses
   * @param mine only the events where the user is a facilitator will be retrieved
   * @return
   */
  def export(brandCode: String, eventId: Long, status: Int, mine: Boolean) = SecuredRestrictedAction(Viewer) {
    implicit request ⇒
      implicit handler ⇒
        Brand.find(brandCode).map { brand ⇒
          val account = request.user.asInstanceOf[LoginIdentity].userAccount
          val events = if (eventId > 0) {
            Event.find(eventId).map { event ⇒
              if (mine) {
                if (event.facilitatorIds.contains(account.personId)) {
                  event :: Nil
                } else {
                  Nil
                }
              } else {
                event :: Nil
              }
              event :: Nil
            }.getOrElse(Nil)
          } else {
            if (mine) {
              Event.findByFacilitator(account.personId, brandCode)
            } else {
              Event.findByParameters(brandCode)
            }
          }
          val evaluations = Evaluation.findByEvents(events.map(e ⇒ e.id.get))
          val filteredEvaluations = if (status >= 0) {
            evaluations.filter(e ⇒ e._3.status.id == status)
          } else {
            evaluations
          }
          val date = LocalDate.now.toString
          Ok.sendFile(
            content = createXLSXreport(filteredEvaluations),
            fileName = _ ⇒ s"report-$date-$brandCode.xlsx")
        }.getOrElse(NotFound("Unknown brand"))
  }

  private def createXLSXreport(evaluations: List[(Event, Person, Evaluation)]): java.io.File = {
    import org.apache.poi.ss.util._
    import org.apache.poi.xssf.usermodel._
    import play.api._

    val wb = new XSSFWorkbook(Play.application.resourceAsStream("reports/evaluations.xlsx").get)
    val sheet = wb.getSheetAt(0)
    var rowNumber = 0
    evaluations.foreach { e ⇒
      rowNumber += 1
      val row = sheet.createRow(rowNumber)
      row.createCell(0).setCellValue(e._1.title)
      row.createCell(1).setCellValue(e._1.schedule.start + " / " + e._1.schedule.end)
      row.createCell(2).setCellValue(e._1.location.city)
      row.createCell(3).setCellValue(e._2.fullName)
      row.createCell(4).setCellValue(e._3.question6)
      row.createCell(5).setCellValue(e._3.question7)
      row.createCell(6).setCellValue(e._3.question1)
      row.createCell(7).setCellValue(e._3.question2)
      row.createCell(8).setCellValue(e._3.question3)
      row.createCell(9).setCellValue(e._3.question4)
      row.createCell(10).setCellValue(e._3.question5)
      row.createCell(11).setCellValue(e._3.question8)
    }
    val tmpFile = File.createTempFile("report", ".xlsx")
    tmpFile.deleteOnExit()
    val os = new FileOutputStream(tmpFile)
    wb.write(os)
    os.close()
    tmpFile
  }

  private def findEvents(account: UserAccount): List[Event] = {
    if (account.editor) {
      Event.findActive
    } else {
      Event.findByCoordinator(account.personId)
    }
  }
}
