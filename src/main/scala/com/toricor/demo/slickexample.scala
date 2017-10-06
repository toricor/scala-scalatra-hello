package com.toricor.demo

import org.scalatra.{FutureSupport, ScalatraBase, ScalatraServlet}
import slick.jdbc.H2Profile.api._

// import scala.concurrent.ExecutionContext.Implicits.global

object Tables {
  // Definition of the USERS table
  class Users(tag: Tag) extends Table[(Int, String)](tag, "USERS") {
    def id = column[Int]("USER_ID", O.PrimaryKey)
    def name = column[String]("USER_NAME")
    def * = (id, name)
  }

  // Definition of the EVENTS table
  class Events(tag: Tag) extends Table[(Int, String, String, Int, String, Int, Int, String, String)](tag, "EVENTS") {
    def id = column[Int]("EVENT_ID", O.PrimaryKey)
    def title = column[String]("TITLE")
    def description = column[String]("DESCRIPTION")
    def author = column[Int]("USER_ID")
    def place = column[String]("PLACE")
    def participants = column[Int]("PARTICIPANTS")
    def max_participants = column[Int]("MAX")
    def created_at = column[String]("CREATED_AT")
    def published_at = column[String]("PUBLISHED_AT")
    def * = (id, title, description, author, place, participants, max_participants, created_at, published_at)

    def user = foreignKey("USER_EVENT_FK", author, users)(_.id)
  }

  // Definition of the RESERVATION table
  class Reservations(tag: Tag) extends Table[(Int, Int, Int)](tag, "RESERVATIONS") {
    def id = column[Int]("RESERVATION_ID", O.PrimaryKey)
    def user_id = column[Int]("USER_ID")
    def event_id = column[Int]("EVENT_ID")
    def * = (id, user_id, event_id)

    def user = foreignKey("USER_RESERVATION_FK", user_id, users)(_.id)
    def event = foreignKey("EVENT_FK", event_id, events)(_.id)
  }

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
    Tables.users += (12, "hoge"),
    Tables.users += (14, "hage"),
    Tables.users += (16, "fuga"),
    Tables.events += (2, "title2", "great project2", 12, "五反田", 22, 24, "2017-10-01 00:00:00", "2017-11-01 00:00:01"),
    Tables.events += (3, "title3", "great project3", 12, "五反田", 20, 240, "2016-10-01 00:00:00", "2016-11-01 00:00:01"),
    Tables.events += (5, "title5", "great project5", 14, "五反田", 2340, 2400, "2014-10-01 00:00:00", "2016-11-01 00:00:01"),
    Tables.reservations += (1, 12, 2),
    Tables.reservations += (2, 14, 3),
    Tables.reservations += (3, 14, 5)
  )

  // DBIO Action which creates the schema
  val createSchemaAction = (users.schema ++ events.schema ++ reservations.schema).create

  // DBIO Action which drops the schema
  val dropSchemaAction = (users.schema ++ events.schema ++ reservations.schema).drop

  // Create database, composing create schema and insert sample data actions
  val createDatabase = DBIO.seq(createSchemaAction, insertUsersEventsAndReservationData)

}

trait SlickRoutes extends ScalatraBase with FutureSupport {

  def db: Database

  get("/db/create-db") {
    db.run(Tables.createDatabase)
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
    db.run(Tables.findEventsWithAuthors.result) map { xs =>
      println(xs)
      contentType = "text/plain"
      xs map { case (s1, s2) => f"  $s1 created by $s2" } mkString "\n"
    }
  }

  get("/reservations/with-events-and-users") {
    db.run(Tables.findReservationsWithUsersAndEvents.result) map { xs =>
      println(xs)
      contentType = "text/plain"
      xs map { case (s1, s2) => f"  $s1 reserved by $s2" } mkString "\n"
    }
  }

}

class SlickApp(val db: Database) extends ScalatraServlet with FutureSupport with SlickRoutes {

  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

}