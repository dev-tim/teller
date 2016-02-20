/*
 * Happy Melly Teller
 * Copyright (C) 2013 - 2016, Happy Melly http://www.happymelly.com
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
 * If you have questions concerning this license or the applicable additional
 * terms, you may contact by email Sergey Kotlov, sergey.kotlov@happymelly.com or
 * in writing Happy Melly One, Handelsplein 37, Rotterdam, The Netherlands, 3071 PR
 */
package controllers.core

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import controllers.{Security, Files, Activities, MemberNotifications, Utilities }
import controllers.Forms._
import models.UserRole.Role._
import models._
import models.payment.{GatewayWrapper, PaymentException, RequestException}
import models.repository.Repositories
import org.joda.time.DateTime
import play.api.Play.current
import play.api.data.Forms._
import play.api.data.validation.Constraints
import play.api.data.{Form, FormError}
import play.api.i18n.{MessagesApi, Messages}
import play.api.mvc._
import play.api.{Logger, Play}
import services.TellerRuntimeEnvironment
import services.integrations.{Email, Integrations}

import scala.collection.mutable
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

case class PeopleDetailConfig(facilitator: Boolean, deletable: Boolean, member: Boolean)

class People @javax.inject.Inject()(override implicit val env: TellerRuntimeEnvironment,
                                    override val messagesApi: MessagesApi,
                                    val services: Repositories,
                                    val email: Email,
                                    deadbolt: DeadboltActions, handlers: HandlerCache, actionBuilder: ActionBuilders)
  extends Security(deadbolt, handlers, actionBuilder, services)(messagesApi, env)
    with Integrations
    with Files
    with Activities
    with MemberNotifications {

  val contentType = "image/jpeg"
  val indexCall: Call = routes.People.index()

  /**
    * Form target for toggling whether a person is active
    *
    * @param id Person identifier
    */
  def activation(id: Long) = RestrictedAction(Admin) { implicit request ⇒ implicit handler ⇒ implicit user ⇒
    services.person.find(id) flatMap {
      case None => redirect(indexCall, "error" -> "Person not found")
      case Some(person) =>
        Form("active" -> boolean).bindFromRequest.fold(
          form ⇒ badRequest("invalid form data"),
          active ⇒ {
            services.person.activate(id, active)
            val log = if (active)
              activity(person, user.person).activated.insert(services)
            else
              activity(person, user.person).deactivated.insert(services)
            redirect(routes.People.details(id), "success" -> log.toString)
          })
    }
  }

  /**
    * Render a Create page
    */
  def add = RestrictedAction(Admin) { implicit request ⇒ implicit handler ⇒ implicit user ⇒
    ok(views.html.v2.person.form(user, None, People.personForm(user.name, None, services)))
  }

  /**
    * Assign a person to an organisation
    */
  def addRelationship() = RestrictedAction(List(Admin, Coordinator)) { implicit request ⇒
    implicit handler ⇒ implicit user ⇒
      val relationshipForm = Form(tuple("page" -> text,
        "personId" -> longNumber,
        "organisationId" -> longNumber))

      relationshipForm.bindFromRequest.fold(
        errors ⇒ badRequest("organisationId missing"),
        { case (page, personId, organisationId) ⇒
          (for {
            p <- services.person.find(personId)
            o <- services.org.find(organisationId)
          } yield (p, o)) flatMap {
            case (None, _) => notFound("Person not found")
            case (_, None) => notFound("Organisation not found")
            case (Some(person), Some(organisation)) =>
              person.addRelation(organisationId, services)

              val log = activity(person, user.person, Some(organisation)).connected.insert(services)
              // Redirect to the page we came from - either the person or organisation details page.
              val action: String = if (page == "person")
                routes.People.details(personId).url
              else
                routes.Organisations.details(organisationId).url
              redirect(action, "success" -> "Person is a member of the organisation now")
          }
        })
  }

  /**
    * Create form submits to this action.
    */
  def create = RestrictedAction(Admin) { implicit request ⇒ implicit handler ⇒ implicit user ⇒
    People.personForm(user.name, None, services).bindFromRequest.fold(
      formWithErrors ⇒ badRequest(views.html.v2.person.form(user, None, formWithErrors)),
      person ⇒ {
        services.person.insert(person) flatMap { updatedPerson =>
          val log = activity(updatedPerson, user.person).created.insert(services)
          redirect(indexCall, "success" -> "New person was added")
        }
      })
  }

  /**
    * Delete a person
    *
    * @param id Person identifier
    */
  def delete(id: Long) = RestrictedAction(Admin) { implicit request ⇒ implicit handler ⇒ implicit user ⇒
    (for {
      p <- services.person.find(id)
      flat <- Person.deletable(id, services)
    } yield (p, flat)) flatMap {
      case (None, _) => notFound("Person not found")
      case (Some(person), false) =>
        redirect(indexCall, "error" -> Messages("error.person.nonDeletable"))
      case (Some(person), true) =>
        services.person.delete(person)
        val log = activity(person, user.person).deleted.insert(services)
        redirect(indexCall, "success" -> "Person was deleted")
    }
  }

  /**
    * Delete a relationthip of a person and an organisation
    *
    * @param page Page identifier where the action was requested from
    * @param personId Person identifier
    * @param organisationId Org identifier
    */
  def deleteRelationship(page: String, personId: Long, organisationId: Long) = ProfileAction(personId) {
    implicit request ⇒ implicit handler ⇒ implicit user ⇒
      (for {
        p <- services.person.find(personId)
        o <- services.org.find(organisationId)
      } yield (p, o)) flatMap {
        case (None, _) => notFound("Person not found")
        case (_, None) => notFound("Organisation not found")
        case (Some(person), Some(organisation))=>
          person.deleteRelation(organisationId, services)
          val log = activity(person, user.person,
            Some(organisation)).disconnected.insert(services)
          // Redirect to the page we came from - either the person or
          // organisation details page.
          val action: String = if (page == "person")
            routes.People.details(personId).url
          else
            routes.Organisations.details(organisationId).url
          redirect(action, "success" -> "Person is no longer a member of the organisation")
      }
  }

  /**
    * Render Details page
    *
    * @param id Person identifier
    */
  def details(id: Long) = RestrictedAction(Viewer) { implicit request ⇒ implicit handler ⇒ implicit user ⇒
    (for {
      p <- services.person.findComplete(id)
      f <- services.facilitator.findByPerson(id)
      m <- services.person.memberships(id)
      o <- services.org.findActive
      deletable <- Person.deletable(id, services)
      member <- services.person.member(id)
    } yield (p, f, m, o, deletable, member)) flatMap {
      case (None, _, _, _, _, _) => redirect(indexCall, "error" -> "Person not found")
      case (Some(person), facilitators, memberships, orgs, deletable, member) =>
        val conf = PeopleDetailConfig(facilitators.nonEmpty, deletable, member.isDefined)
        val otherOrganisations = orgs.filterNot(organisation ⇒ memberships.contains(organisation))
        val badgesInfo = if (conf.facilitator) {
          val query = for {
            badges <- services.brandBadge.find(facilitators.flatMap(_.badges))
            brands <- services.brand.find(badges.map(_.brandId).distinct)
          } yield (badges, brands)
          query map { case (badges, brands) =>
            badges.map { badge =>
              (badge, brands.find(_.brand.identifier == badge.brandId).map(_.brand.name).getOrElse(""))
            }
          }
        } else {
          Future.successful(List())
        }
        badgesInfo flatMap { info =>
          ok(views.html.v2.person.details(user, person, memberships, otherOrganisations, conf, info, member))
        }
    }
  }

  /**
    * Render an Edit page
    *
    * @param id Person identifier
    */
  def edit(id: Long) = DynamicAction(ProfileEditor, id) { implicit request ⇒
    implicit handler ⇒ implicit user ⇒
      services.person.findComplete(id) flatMap {
        case None => notFound("Person not found")
        case Some(person) ⇒
          ok(views.html.v2.person.form(user, Some(id), People.personForm(user.name, None, services).fill(person)))
      }
  }

  /**
    * Edit form submits to this action
    *
    * @param id Person identifier
    */
  def update(id: Long) = DynamicAction(ProfileEditor, id) { implicit request ⇒
    implicit handler ⇒ implicit user ⇒
      services.person.findComplete(id) flatMap {
        case None => notFound("Person not found")
        case Some(oldPerson) ⇒
          People.personForm(user.name, Some(id), services).bindFromRequest.fold(
            errors ⇒ badRequest(views.html.v2.person.form(user, Some(id), errors)),
            person ⇒ {
              val modified = resetReadOnlyAttributes(person, oldPerson)

              services.person.member(id).filter(_.isDefined) map { _ =>
                val msg = connectMeMessage(oldPerson.profile, modified.profile)
                msg foreach { x => slack.send(updateMsg(modified.fullName, x)) }
              }
              services.person.update(modified) flatMap { _ =>
                if (modified.email != oldPerson.email) {
                  services.identity.findByEmail(oldPerson.email).filter(_.isDefined) map { identity =>
                    if (identity.get.userId.exists(_ == id)) {
                      services.identity.delete(oldPerson.email)
                      services.identity.insert(identity.get.copy(email = modified.email))
                    }
                  }
                }
                val log = activity(modified, user.person).updated.insert(services)
                val url: String = routes.People.details(id).url
                redirect(url, "success" -> "Person was updated")
              }
            })
      }
  }

  /**
    * Renders tab for the given person
    *
    * @param id Person or Member identifier
    * @param tab Tab identifier
    */
  def renderTabs(id: Long, tab: String) = RestrictedAction(Viewer) {
    implicit request ⇒ implicit handler ⇒ implicit user ⇒
      tab match {
        case "contributions" ⇒
          services.contribution.contributions(id, isPerson = true) flatMap { contributions =>
            ok(views.html.v2.element.contributions("person", contributions))
          }
        case "experience" ⇒
          (for {
            person <- services.person.find(id)
            experience <- retrieveByBrandStatistics(id)
            endorsements <- services.person.endorsements(id)
            materials <- services.person.materials(id)
          } yield (person, experience, endorsements, materials)) flatMap {
            case (None, _, _, _) => notFound("Person not found")
            case (Some(person), experience, endorsements, materials) =>
              val endorsementsWithExp = endorsements.map {x =>
                (x, experience.find(_._1 == x.brandId).map(_._2).getOrElse(""))
              }
              val sortedMaterials = materials.sortBy(_.linkType)
              ok(views.html.v2.person.tabs.experience(person, experience, endorsementsWithExp, sortedMaterials))
          }
        case "facilitation" ⇒
          (for {
            p <- services.person.find(id)
            l <- services.license.licensesWithBrands(id)
            f <- services.facilitator.findByPerson(id)
            langs <- services.facilitator.languages(id)
            c <- services.facilitator.countries(id)
          } yield (p, l, f, langs, c)) flatMap {
            case (None, _, _, _, _) => notFound("Person not found")
            case (Some(person), licenses, facilitation, languages, countries) =>
              val facilitatorData = licenses
                .map(x ⇒ (x, facilitation.find(_.brandId == x.license.brandId).get.publicRating))
              ok(views.html.v2.person.tabs.facilitation(person, facilitatorData, languages, countries))
          }
        case "membership" ⇒
          (for {
            person <- services.person.find(id)
            payments <- services.paymentRecord.findByPerson(id)
            member <- services.person.member(id)
          } yield (person, payments, member)) flatMap {
            case (_, _, None) => ok("Person is not a member")
            case (None, _, _) => notFound("Person not found")
            case (Some(person), payments, Some(member)) =>
              ok(views.html.v2.person.tabs.membership(user, person, member, payments))
          }
        case _ ⇒ ok("")
      }
  }


  /**
    * Render a list of people in the network
    *
    * @return
    */
  def index = RestrictedAction(Viewer) { implicit request ⇒ implicit handler ⇒ implicit user ⇒
    services.person.findAll flatMap { people =>
      ok(views.html.v2.person.index(user, people))
    }
  }

  /**
    * Cancels a subscription for yearly-renewing membership
    *
    * @param id Person id
    */
  def cancel(id: Long) = ProfileAction(id) { implicit request ⇒ implicit handler ⇒ implicit user ⇒
    val url: String = routes.People.details(id).url + "#membership"
    (for {
      p <- services.person.find(id)
      m <- services.person.member(id)
    } yield (p, m)) flatMap {
      case (None, _) => notFound("Person not found")
      case (_, None) => redirect(url, "error" -> Messages("error.membership.noSubscription"))
      case (Some(person), Some(member)) =>
        if (member.renewal) {
          val key = Play.configuration.getString("stripe.secret_key").get
          val gateway = new GatewayWrapper(key)
          try {
            gateway.cancel(person.customerId.get)
            services.member.update(member.copy(renewal = false))
          } catch {
            case e: PaymentException ⇒
              Redirect(url).flashing("error" -> Messages(e.msg))
            case e: RequestException ⇒
              e.log.foreach(Logger.error(_))
              Redirect(url).flashing("error" -> Messages(e.getMessage))
          }
          redirect(url, "success" -> "Subscription was successfully canceled")
        } else {
          redirect(url, "error" -> Messages("error.membership.noSubscription"))
        }    }
  }

  /**
    * Compares social profiles and returns a list of errors for a form
    *
    * @param left Social profile object
    * @param right Social profile object
    */
  protected def compareSocialProfiles(left: SocialProfile, right: SocialProfile): List[FormError] = {

    val list = mutable.MutableList[FormError]()
    val msg = Messages("error.socialProfile.exist")
    if (left.twitterHandle.nonEmpty && left.twitterHandle == right.twitterHandle) {
      list += FormError("profile.twitterHandle", msg)
    }
    if (left.facebookUrl.nonEmpty && left.facebookUrl == right.facebookUrl) {
      list += FormError("profile.facebookUrl", msg)
    }
    if (left.linkedInUrl.nonEmpty && left.linkedInUrl == right.linkedInUrl) {
      list += FormError("profile.linkedInUrl", msg)
    }
    if (left.googlePlusUrl.nonEmpty && left.googlePlusUrl == right.googlePlusUrl) {
      list += FormError("profile.googlePlusUrl", msg)
    }
    list.toList
  }


  /**
    * Retrieve facilitator statistics by brand, including years of experience,
    *  number of events and rating
    *
    * @param id Facilitator id
    */
  protected def retrieveByBrandStatistics(id: Long) = {
    (for {
      l <- services.license.licensesWithBrands(id)
      f <- services.facilitator.findByPerson(id)
    } yield (l, f)) map { case (licenses, facilitations) =>
      licenses.sortBy(_.brand.name).map { view ⇒
        val facilitator = facilitations.find(_.brandId == view.brand.identifier).get
        (facilitator, view.brand.name)
      }
    }
  }

  /**
    * Updates the attributes of a person that shouldn't be changed on update
    *
    * @param updated Updated object
    * @param existing Existing object
    * @param user Current active user
    */
  protected def resetReadOnlyAttributes(updated: Person, existing: Person)(implicit user: ActiveUser): Person = {
    val modified = updated
      .copy(id = existing.id, active = existing.active)
      .copy(photo = existing.photo, customerId = existing.customerId)
      .copy(addressId = existing.addressId)
    val modifiedWithEmail = if (user.person.identifier == existing.identifier && user.account.byEmail)
      modified.copy(email = existing.email)
    else
      modified
    modifiedWithEmail
  }

  protected def updateMsg(name: String, msg: String): String = {
    "%s updated her/his social profile. %s".format(name, msg)
  }
}

object People {

  def pictureUrl(person: Person): String =
    person.photo.url.getOrElse(Utilities.fullUrl(controllers.routes.Assets.at("images/happymelly-face-white.png").url))

  /**
    * HTML form mapping for a person’s address.
    */
  val addressMapping = mapping(
    "id" -> ignored(Option.empty[Long]),
    "street1" -> optional(text),
    "street2" -> optional(text),
    "city" -> optional(text),
    "province" -> optional(text),
    "postCode" -> optional(text),
    "country" -> nonEmptyText)(Address.apply)(Address.unapply)

  /**
    * HTML form mapping for a person’s social profile.
    */
  val socialProfileMapping = mapping(
    "twitterHandle" -> optional(text.verifying(Constraints.pattern("""[A-Za-z0-9_]{1,16}""".r, error = "error.twitter"))),
    "facebookUrl" -> optional(facebookProfileUrl),
    "linkedInUrl" -> optional(linkedInProfileUrl),
    "googlePlusUrl" -> optional(googlePlusProfileUrl))({
    (twitterHandle, facebookUrl, linkedInUrl, googlePlusUrl) ⇒
      SocialProfile(0, ProfileType.Person, twitterHandle, facebookUrl, linkedInUrl, googlePlusUrl)
  })({
    (s: SocialProfile) ⇒
      Some(s.twitterHandle, s.facebookUrl,
        s.linkedInUrl, s.googlePlusUrl)
  })

  /**
    * HTML form mapping for creating and editing.
    */
  def personForm(editorName: String, userId: Option[Long] = None, services: Repositories)(implicit user: ActiveUser) = {
    Form(mapping(
      "id" -> ignored(Option.empty[Long]),
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "emailAddress" -> play.api.data.Forms.email.verifying("Email address is already in use", { suppliedEmail =>
        import scala.concurrent.duration._
        Await.result(services.identity.checkEmail(suppliedEmail, userId), 10.seconds)
      }),
      "birthday" -> optional(jodaLocalDate),
      "signature" -> boolean,
      "address" -> addressMapping,
      "bio" -> optional(text),
      "interests" -> optional(text),
      "webSite" -> optional(webUrl),
      "blog" -> optional(webUrl),
      "active" -> ignored(true),
      "dateStamp" -> mapping(
        "created" -> ignored(DateTime.now()),
        "createdBy" -> ignored(editorName),
        "updated" -> ignored(DateTime.now()),
        "updatedBy" -> ignored(editorName))(DateStamp.apply)(DateStamp.unapply))(
      { (id, firstName, lastName, emailAddress, birthday, signature,
         address, bio, interests, webSite, blog, active, dateStamp) ⇒
      {
        val person = Person(id, firstName, lastName, emailAddress, birthday, Photo.empty,
          signature, address.id.getOrElse(0), bio, interests,
          webSite, blog, customerId = None, virtual = false, active = active, dateStamp = dateStamp)
        person.address_=(address)
        person
      }
      })(
      { (p: Person) ⇒
        Some(
          (p.id, p.firstName, p.lastName, p.email, p.birthday,
            p.signature, p.address, p.bio, p.interests, p.webSite, p.blog, p.active, p.dateStamp))
      }))
  }
}