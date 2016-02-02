/*
 * Happy Melly Teller
 * Copyright (C) 2013 - 2015, Happy Melly http://www.happymelly.com
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
 * If you have questions concerning this license or the applicable additional terms,
 * you may contact by email Sergey Kotlov, sergey.kotlov@happymelly.com or
 * in writing Happy Melly One, Handelsplein 37, Rotterdam, The Netherlands, 3071 PR
 */
package controllers

import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltActions}
import models.Material
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{MessagesApi, I18nSupport, Messages}
import play.api.libs.json.{JsValue, Json, Writes}
import services.TellerRuntimeEnvironment

class Materials @Inject() (override implicit val env: TellerRuntimeEnvironment,
                           val messagesApi: MessagesApi,
                           deadbolt: DeadboltActions, handlers: HandlerCache, actionBuilder: ActionBuilders)
  extends Security(deadbolt, handlers, actionBuilder)
  with I18nSupport {

  implicit val MaterialWrites = new Writes[Material] {
    def writes(link: Material): JsValue = {
      Json.obj(
        "personId" -> link.personId,
        "linkType" -> link.linkType.capitalize,
        "link" -> link.link,
        "brandId" -> link.brandId,
        "id" -> link.id.get)
    }
  }

  /**
   * Adds new material if the link is valid
   *
   * @param personId Person identifier
   */
  def create(personId: Long) = AsyncSecuredProfileAction(personId) { implicit request ⇒
    implicit handler ⇒ implicit user ⇒
      personService.find(personId) flatMap {
        case None => jsonNotFound(Messages("error.person.notFound"))
        case Some(person) =>
          val form = Form(tuple("brandId" -> longNumber, "type" -> nonEmptyText, "url" -> nonEmptyText))
          form.bindFromRequest.fold(
            error ⇒ jsonBadRequest("Link cannot be empty"),
            materialData ⇒ {
              val material = Material(None, personId, materialData._1, materialData._2, materialData._3)
              personService.insertMaterial(Material.updateType(material)) flatMap { link =>
                jsonOk(Json.toJson(link))
              }
            })
      }
  }

  /**
   * Deletes the given material if the material exists and is belonged to
   * the given person
   *
   * Person identifier is used to check access rights
   *
   * @param personId Person identifier
   * @param id Material identifier
   */
  def remove(personId: Long, id: Long) = AsyncSecuredProfileAction(personId) {
    implicit request ⇒ implicit handler ⇒ implicit user ⇒
      personService.deleteMaterial(personId, id) flatMap { _ =>
        jsonSuccess("ok")
      }
  }

}
