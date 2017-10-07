package com.toricor.demo

import org.json4s.{Extraction, JObject}
import org.json4s.JsonAST.JNothing
import org.scalatra._

// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

case class Flower (slug: String, name: String)

object FlowerData {
  var all = List(
    Flower("yellow-tulip", "Yellow Tulip"),
    Flower("red-rose", "Red Rose"),
    Flower("black-rose", "Black Rose")
  )
}

class FlowersController extends ScalatraServlet with JacksonJsonSupport{
  // Sets up automatic case class to JSON output serialization , required by
  // the JValueResult trait.
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  // "/flowers/*"
  get("/sample") {
    Extraction.decompose(
      Flower("blue-rose", "Blue Rose")
    )
  }

  get("/all") {
    FlowerData.all
  }

  post("/post") {
    parsedBody match {
      case JNothing => halt(400, "invalid json")
      case json: JObject => {
        val fl: Flower = json.extract[Flower]
        fl.slug
      }
      case _ => halt(400, "unknown json")
    }
  }
}