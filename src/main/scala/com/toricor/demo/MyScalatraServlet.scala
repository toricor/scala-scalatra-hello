package com.toricor.demo

import org.scalatra._

class MyScalatraServlet extends ScalatraServlet {

  get("/") {
    views.html.hello()
  }

  get("/hello") {
    "this message will be json"
  }

}
