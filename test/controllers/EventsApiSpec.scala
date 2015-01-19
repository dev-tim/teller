/*
* Happy Melly Teller
* Copyright (C) 2013 - 2015, Happy Melly http -> //www.happymelly.com
*
* This file is part of the Happy Melly Teller.
*
* Happy Melly Teller is free software ->  you can redistribute it and/or modify
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
* along with Happy Melly Teller.  If not, see <http -> //www.gnu.org/licenses/>.
*
* If you have questions concerning this license or the applicable additional
* terms, you may contact by email Sergey Kotlov, sergey.kotlov@happymelly.com
* or in writing Happy Melly One, Handelsplein 37, Rotterdam,
* The Netherlands, 3071 PR
*/
package controllers

import org.specs2.mutable._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.libs.json._
import play.api.mvc._
import stub.{ FakeServices, FakeApiAuthentication }
import scala.concurrent.Future

class EventsApiSpec extends Specification {

  /** Test controller without api authentication and with stubbed services */
  class TestEventsApi() extends EventsApi
    with FakeApiAuthentication
    with FakeServices

  /** Test controller with api authentication and with stubbed services */
  class AnotherTestEventsApi() extends EventsApi
    with ApiAuthentication
    with FakeServices

  "Event details API call" should {
    "return event details in JSON format" in {
      val controller = new TestEventsApi()
      val result: Future[SimpleResult] = controller.event(1).apply(FakeRequest())
      status(result) must equalTo(OK)
      contentType(result) must beSome("text/plain")
      charset(result) must beSome("utf-8")
      contentAsJson(result) mustEqual Json.obj(
        "brand" -> "TEST",
        "type" -> 1,
        "title" -> "Test event",
        "description" -> None.asInstanceOf[Option[String]],
        "spokenLanguages" -> Json.arr("German"),
        "materialsLanguage" -> None.asInstanceOf[Option[String]],
        "specialAttention" -> None.asInstanceOf[Option[String]],
        "start" -> "2015-01-19",
        "end" -> "2015-01-19",
        "hoursPerDay" -> 1,
        "totalHours" -> 1,
        "city" -> "spb",
        "country" -> "RU",
        "website" -> None.asInstanceOf[Option[String]],
        "registrationPage" -> None.asInstanceOf[Option[String]],
        "public" -> true,
        "archived" -> false)
    }
    "return 404 error with error message when an event doesn't exist" in {
      val controller = new TestEventsApi()
      val result: Future[SimpleResult] = controller.event(101).apply(FakeRequest())
      status(result) must equalTo(NOT_FOUND)
      contentType(result) must beSome("text/plain")
      contentAsString(result) mustEqual "Unknown event"
    }
    "return 401 error if api_token is not provided" in {

      val controller = new AnotherTestEventsApi()
      val result: Future[SimpleResult] = controller.event(1).apply(FakeRequest())
      status(result) must equalTo(UNAUTHORIZED)
      contentAsString(result) mustEqual "Unauthorized"
    }
  }

  "Events API call" should {

  }

}
