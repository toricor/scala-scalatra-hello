package com.toricor.demo

import org.scalatra.test.scalatest._
import org.scalatest.FunSuiteLike
import slick.jdbc.H2Profile.api._

class SlickAppTests extends ScalatraSuite with FunSuiteLike{

  implicit val db = new Database
  addServlet(new SlickApp(db), "/*")

  test("simple get"){
    get("/hello"){
      status should equal (200)
      body should include ("hello!")
    }
  }

}
