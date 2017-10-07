package com.toricor.demo

import org.scalatra._

case class Flower (slug: String, name: String)

object FlowerData {
  var all = List(
    Flower("yellow-tulip", "Yellow Tulip"),
    Flower("red-rose", "Red Rose"),
    Flower("black-rose", "Black Rose")
  )
}

class FlowersController extends ScalatraServlet {
  // "/flowers/*"
  get("/all") {
    FlowerData.all
  }
}