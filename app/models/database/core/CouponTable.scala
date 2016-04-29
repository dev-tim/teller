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

package models.database.core

import com.github.tototoshi.slick.MySQLJodaSupport._
import models.core.Coupon
import org.joda.time.LocalDate
import slick.driver.JdbcProfile

/**
  * Represents coupon table
  */
private[models] trait CouponTable {

  protected val driver: JdbcProfile
  import driver.api._

  class Coupons(tag: Tag) extends Table[Coupon](tag, "COUPON") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def code = column[String]("CODE", O.Length(15, varying = true))
    def discount = column[Int]("DISCOUNT")
    def start = column[Option[LocalDate]]("START")
    def end = column[Option[LocalDate]]("END")

    def * = (id.?, code, discount, start, end) <> ((Coupon.apply _).tupled, Coupon.unapply)
  }
}