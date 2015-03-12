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
 * If you have questions concerning this license or the applicable additional
 * terms, you may contact by email Sergey Kotlov, sergey.kotlov@happymelly.com or
 * in writing Happy Melly One, Handelsplein 37, Rotterdam, The Netherlands, 3071 PR
 */

package controllers.acceptance

import controllers.api.{ ApiAuthentication, MembersApi }
import org.specs2.mutable._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import stubs.FakeServices

class MembersApiSpec extends Specification {

  /** Test controller with api authentication and with stubbed services */
  class AnotherTestMembersApi() extends MembersApi
    with ApiAuthentication
    with FakeServices

  override def is = s2"""

  If api_token is not provided 401 error should be returned
    on 'members' call                                        $e1
    on 'member' call                                         $e2
  """

  def e1 = {
    val controller = new AnotherTestMembersApi()
    val result = controller.members().apply(FakeRequest())
    status(result) must equalTo(UNAUTHORIZED)
    contentAsString(result) mustEqual "Unauthorized"
  }

  def e2 = {
    val controller = new AnotherTestMembersApi()
    val result = controller.member(1L).apply(FakeRequest())
    status(result) must equalTo(UNAUTHORIZED)
    contentAsString(result) mustEqual "Unauthorized"
  }

}