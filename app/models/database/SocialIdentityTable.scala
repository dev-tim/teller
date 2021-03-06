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

package models.database

import models.SocialIdentity
import play.api.libs.json.{JsValue, Json}
import securesocial.core.{AuthenticationMethod, BasicProfile, OAuth1Info, OAuth2Info}
import slick.driver.JdbcProfile

import scala.language.implicitConversions

private[models] trait SocialIdentityTable {

  protected val driver: JdbcProfile
  import driver.api._

  /**
    * `LoginIdentity` database table mapping.
    */
  class SocialIdentities(tag: Tag) extends Table[SocialIdentity](tag, "SOCIAL_IDENTITY") {

    implicit def string2AuthenticationMethod = MappedColumnType.base[AuthenticationMethod, String](
      authenticationMethod ⇒ authenticationMethod.method,
      string ⇒ AuthenticationMethod(string))

    implicit def tuple2OAuth1Info(tuple: (Option[String], Option[String])) = tuple match {
      case (Some(token), Some(secret)) ⇒ Some(OAuth1Info(token, secret))
      case _ ⇒ None
    }

    implicit def tuple2OAuth2Info(tuple: (Option[String], Option[String], Option[Int], Option[String])) = tuple match {
      case (Some(token), tokenType, expiresIn, refreshToken) ⇒ Some(OAuth2Info(token, tokenType, expiresIn, refreshToken))
      case _ ⇒ None
    }

    def uid = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def userId = column[String]("USER_ID")
    def providerId = column[String]("PROVIDER_ID")
    def email = column[Option[String]]("EMAIL")
    def firstName = column[Option[String]]("FIRST_NAME")
    def lastName = column[Option[String]]("LAST_NAME")
    def fullName = column[Option[String]]("FULL_NAME")
    def authMethod = column[AuthenticationMethod]("AUTH_METHOD")
    def avatarUrl = column[Option[String]]("AVATAR_URL")
    def secret = column[Option[String]]("SECRET")
    def tokenType = column[Option[String]]("TOKEN_TYPE")
    def expiresIn = column[Option[Int]]("EXPIRES_IN")
    def refreshToken = column[Option[String]]("REFRESH_TOKEN")
    def apiToken = column[String]("API_TOKEN")
    def extraInfo = column[Option[String]]("EXTRA_INFO")

    // oAuth 1
    def token = column[Option[String]]("TOKEN")

    // oAuth 2
    def accessToken = column[Option[String]]("ACCESS_TOKEN")

    type SocialIdentitiesFields = (Option[Long], String, String, Option[String], Option[String],
      Option[String], Option[String], Option[String], AuthenticationMethod, Option[String], Option[String],
      Option[String], Option[String], Option[Int], Option[String], String, Option[String])

    def * = (uid.?,
      userId, providerId,
      firstName,
      lastName,
      fullName,
      email,
      avatarUrl,
      authMethod,
      token, secret,
      accessToken, tokenType, expiresIn, refreshToken,
      apiToken, extraInfo) <>(
      (u: SocialIdentitiesFields) ⇒ SocialIdentity(u._1, BasicProfile(u._2, u._3, u._4, u._5,
        u._6, u._7, u._8, u._9, (u._10, u._11), (u._12, u._13, u._14, u._15),
        None, u._17.map(x => Json.parse(x))), u._16),
      (u: SocialIdentity) ⇒ {
        Some((u.uid, u.profile.userId, u.profile.providerId, u.profile.firstName,
          u.profile.lastName, u.profile.fullName, u.profile.email,
          u.profile.avatarUrl, u.profile.authMethod,
          u.profile.oAuth1Info.map(_.token), u.profile.oAuth1Info.map(_.secret),
          u.profile.oAuth2Info.map(_.accessToken), u.profile.oAuth2Info.flatMap(_.tokenType),
          u.profile.oAuth2Info.flatMap(_.expiresIn),
          u.profile.oAuth2Info.flatMap(_.refreshToken),
          u.apiToken, u.profile.extraInfo.map(_.toString)))
      })

  }

}