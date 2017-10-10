package com.toricor.demo

import com.toricor.demo.Tables.Users
import org.json4s.JsonAST._
import org.json4s.{DefaultFormats, Formats, JObject}
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.{FutureSupport, ScalatraBase, ScalatraServlet}
import slick.jdbc.H2Profile.api._

// import scala.concurrent.ExecutionContext.Implicits.global

object Tables {
  // Definition of the USERS table
  class Users(tag: Tag) extends Table[(Int, String)](tag, "USERS") {
    def id = column[Int]("USER_ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("USER_NAME")
    def * = (id, name)
  }

  // Definition of the EVENTS table
  class Events(tag: Tag) extends Table[(Int, String, String, Int, String, Int, Int, String, String)](tag, "EVENTS") {
    def id = column[Int]("EVENT_ID", O.PrimaryKey, O.AutoInc)
    def title = column[String]("TITLE")
    def description = column[String]("DESCRIPTION")
    def author = column[Int]("USER_ID")
    def place = column[String]("PLACE")
    def participants = column[Int]("PARTICIPANTS")
    def max_participants = column[Int]("MAX")
    def created_at = column[String]("CREATED_AT")
    def published_at = column[String]("PUBLISHED_AT")
    def * = (id, title, description, author, place, participants, max_participants, created_at, published_at)

    def user = foreignKey("EVENT_USER_FK", author, users)(_.id)
  }

  // Definition of the RESERVATION table
  class Reservations(tag: Tag) extends Table[(Int, Int, Int)](tag, "RESERVATIONS") {
    def id = column[Int]("RESERVATION_ID", O.PrimaryKey, O.AutoInc)
    def user_id = column[Int]("USER_ID")
    def event_id = column[Int]("EVENT_ID")
    def * = (id, user_id, event_id)

    def user = foreignKey("RESERVE_USER_FK", user_id, users)(_.id)
    def event = foreignKey("RESERVE_EVENT_FK", event_id, events)(_.id)
  }

  // DBアクセサ層はこれが参考になりそう
  // * Slick.MySQLDriverを使っている点
  // * 生のSQLを書いている点
  // https://github.com/hatena/scala-Intern-Bookmark/blob/master/src/main/scala/internbookmark/repository/Users.scala
  // Table query for the USERS table, represents all tuples
  val users = TableQuery[Users]
  val events = TableQuery[Events]
  val reservations = TableQuery[Reservations]

  // find all data
  val findAllUsers = users.map(_.*)
  val findAllEvents = events.map(_.*)
  val findAllReservation = reservations.map(_.*)

  // Query, implicit inner join events and users, return their names
  val findEventsWithAuthors = {
    for {
      e <- events
      u <- e.user
    } yield (e.title, u.name)
  }

  val findReservationsWithUsersAndEvents = {
    for {
      r <- reservations
      e <- r.event
      u <- r.user
    } yield (e.title, u.name)
  }

  // DBIO Action which runs several queries inserting sample data
  val insertUsersEventsAndReservationData = DBIO.seq(
    Tables.users += (1, "hoge"),
    Tables.users += (2, "hage"),
    Tables.users += (3, "fuga"),
    Tables.events += (1, "title2", "great project2", 2, "五反田", 22, 24, "2017-10-01 00:00:00", "2017-11-01 00:00:01"),
    Tables.events += (2, "title3", "great project3", 2, "五反田", 20, 240, "2016-10-01 00:00:00", "2016-11-01 00:00:01"),
    Tables.events += (3, "title4", "great project5", 3, "五反田", 2340, 2400, "2014-10-01 00:00:00", "2016-11-01 00:00:01"),
    Tables.reservations += (1, 1, 2),
    Tables.reservations += (2, 1, 3),
    Tables.reservations += (3, 2, 3)
  )

  // DBIO Action which creates the schema
  val createSchemaAction = (users.schema ++ events.schema ++ reservations.schema).create

  // DBIO Action which drops the schema
  val dropSchemaAction = (users.schema ++ events.schema ++ reservations.schema).drop

  // Create database, composing create schema and insert sample data actions
  val createDatabase = DBIO.seq(createSchemaAction, insertUsersEventsAndReservationData)

}

trait SlickRoutes extends ScalatraBase with FutureSupport with JacksonJsonSupport {

  def db: Database
  protected implicit lazy val jsonFormats: Formats = DefaultFormats
  db.run(Tables.createDatabase)

  before() {
    contentType = formats("json")
  }

  get("/hello") {
    "hello!"
  }

  get("/db/drop-db") {
    db.run(Tables.dropSchemaAction)
  }

  get("/users") {
    db.run(Tables.findAllUsers.result)
  }

  get("/events") {
    db.run(Tables.findAllEvents.result)
  }

  get("/reservations") {
    db.run(Tables.findAllReservation.result)
  }

  get("/events/with-authors") {
    db.run(Tables.findEventsWithAuthors.result)
  }

  get("/reservations/with-events-and-users") {
    db.run(Tables.findReservationsWithUsersAndEvents.result) map { xs =>
      println(xs)
      contentType = "text/plain"
      xs map { case (s1, s2) => f"  $s1 reserved by $s2" } mkString "\n"
    }
  }

  post("/post") {
    //println(parsedBody)
    parsedBody match {
      case JNothing => halt(400, "invalid json")
      case json: JObject => {
        for {
          JObject(user) <- json
          JField("id", JInt(id)) <- user
          JField("name", JString(name)) <- user
        } yield (id, name)
      }
      case _ => halt(400, "unknown json")
    }
  }
}

class SlickApp(val db: Database) extends ScalatraServlet with FutureSupport with SlickRoutes {

  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

}