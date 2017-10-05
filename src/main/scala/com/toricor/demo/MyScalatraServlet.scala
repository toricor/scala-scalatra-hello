package com.toricor.demo

import org.scalatra._

class MyScalatraServlet extends ScalatraServlet {
  // routeのmatchチェックは下から読まれる

  get("/") {
    views.html.hello()
  }

  // match only when you're 127.0.0.1(localhost)
  get("/hello/:name", request.getRemoteHost == "127.0.0.1") {
    <p>Hello, {params("name")}</p>
  }


  get("/hello/redirect") {
    halt(status = 301, headers = Map("Location" -> "http://example.org/"))
  }

  get("/api/users") {
    "this message will be json"
  }

  get("/api/users/:id") {
    halt(status = 301, headers = Map("Location" -> "http://example.org/"))
  }

  post("/api/users/:id") {
    ""
  }

}
