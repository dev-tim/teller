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

package libs.mailchimp

import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * Contains to/from json converters
  */
object Converter {

  implicit val apiErrorReads: Reads[ApiError] = (
    (__ \ "type").read[String] and
      (__ \ "title").read[String] and
      (__ \ "status").read[Int] and
      (__ \ "detail").read[String] and
      (__ \ "instance").read[String]
    )(ApiError)

  implicit val apiErrorWrites: Writes[ApiError] = (
    (__ \ "type").write[String] and
      (__ \ "title").write[String] and
      (__ \ "status").write[Int] and
      (__ \ "detail").write[String] and
      (__ \ "instance").write[String]
    )(unlift(ApiError.unapply))

  implicit val apiErrorFormat: Format[ApiError] = Format(apiErrorReads, apiErrorWrites)

  implicit val campaignDefaultsReads: Reads[CampaignDefaults] = (
    (__ \ "from_name").read[String] and
      (__ \ "from_email").read[String] and
      (__ \ "subject").read[String] and
      (__ \ "language").read[String]
    )(CampaignDefaults)

  implicit val campaignDefaultsWrites: Writes[CampaignDefaults] = (
    (__ \ "from_name").write[String] and
      (__ \ "from_email").write[String] and
      (__ \ "subject").write[String] and
      (__ \ "language").write[String]
    )(unlift(CampaignDefaults.unapply))

  implicit val contactReads: Reads[ListContact] = Json.reads[ListContact]
  implicit val contactWrites: Writes[ListContact] = Json.writes[ListContact]

  implicit val listReads: Reads[List] = (
    (__ \ "id").readNullable[String] and
      (__ \ "name").read[String] and
      (__ \ "contact").read[ListContact] and
      (__ \ "permission_reminder").read[String] and
      (__ \ "use_archive_bar").readNullable[Boolean] and
      (__ \ "campaign_defaults").read[CampaignDefaults] and
      (__ \ "notify_on_subscribe").readNullable[String] and
      (__ \ "notify_on_unsubscribe").readNullable[String] and
      (__ \ "date_created").readNullable[String] and
      (__ \ "list_rating").readNullable[Int] and
      (__ \ "email_type_option").read[Boolean] and
      (__ \ "subscribe_url_short").readNullable[String] and
      (__ \ "subscribe_url_long").readNullable[String] and
      (__ \ "beamer_address").readNullable[String] and
      (__ \ "visibility").readNullable[String]
    )(List)

  implicit val listWrites: Writes[List] = (
    (__ \ "id").writeNullable[String] and
      (__ \ "name").write[String] and
      (__ \ "contact").write[ListContact] and
      (__ \ "permission_reminder").write[String] and
      (__ \ "use_archive_bar").writeNullable[Boolean] and
      (__ \ "campaign_defaults").write[CampaignDefaults] and
      (__ \ "notify_on_subscribe").writeNullable[String] and
      (__ \ "notify_on_unsubscribe").writeNullable[String] and
      (__ \ "date_created").writeNullable[String] and
      (__ \ "list_rating").writeNullable[Int] and
      (__ \ "email_type_option").write[Boolean] and
      (__ \ "subscribe_url_short").writeNullable[String] and
      (__ \ "subscribe_url_long").writeNullable[String] and
      (__ \ "beamer_address").writeNullable[String] and
      (__ \ "visibility").writeNullable[String]
    )(unlift(List.unapply))

  val listFormats: Format[List] = Format(listReads, listWrites)

  implicit val mergeField: Reads[MergeField] = (
    (__ \ "id").readNullable[Int] and
      (__ \ "tag").readNullable[String] and
      (__ \ "name").read[String] and
      (__ \ "type").read[String] and
      (__ \ "required").readNullable[Boolean] and
      (__ \ "default_value").readNullable[String] and
      (__ \ "public").readNullable[Boolean] and
      (__ \ "display_order").readNullable[Int]
    )(MergeField)

}
