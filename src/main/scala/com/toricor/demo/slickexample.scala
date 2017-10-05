package com.toricor.demo

import java.time.LocalDateTime

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

    def user = foreignKey("USER_FK", author, users)
  }

  // Definition of the RESERVATION table
  class Reservations(tag: Tag) extends Table[(Int, Int, Int)](tag, "RESERVATIONS") {
    def id = column[Int]("RESERVATION_ID", O.PrimaryKey)
    def user_id = column[Int]("USER_ID")
    def event_id = column[Int]("EVENT_ID")
    def * = (id, user_id, event_id)

    def user = foreignKey("USER_FK", user_id, users)
    def event = foreignKey("EVENT_FK", event_id, events)
  }

  // Definition of the SUPPLIERS table
  class Suppliers(tag: Tag) extends Table[(Int, String, String, String, String, String)](tag, "SUPPLIERS") {
    def id = column[Int]("SUP_ID", O.PrimaryKey) // This is the primary key column
    def name = column[String]("SUP_NAME")
    def street = column[String]("STREET")
    def city = column[String]("CITY")
    def state = column[String]("STATE")
    def zip = column[String]("ZIP")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (id, name, street, city, state, zip)
  }

  // Definition of the COFFEES table
  class Coffees(tag: Tag) extends Table[(String, Int, Double, Int, Int)](tag, "COFFEES") {
    def name = column[String]("COF_NAME", O.PrimaryKey)
    def supID = column[Int]("SUP_ID")
    def price = column[Double]("PRICE")
    def sales = column[Int]("SALES")
    def total = column[Int]("TOTAL")
    def * = (name, supID, price, sales, total)

    // A reified foreign key relation that can be navigated to create a join
    def supplier = foreignKey("SUP_FK", supID, suppliers)(_.id)
  }
  val users = TableQuery[Users]
  val events = TableQuery[Events]
  // Table query for the SUPPLIERS table, represents all tuples
  val suppliers = TableQuery[Suppliers]

  // Table query for the COFFEES table
  val coffees = TableQuery[Coffees]

  // Query, implicit inner join coffes and suppliers, return their names
  val findCoffeesWithSuppliers = {
    for {
      c <- coffees
      s <- c.supplier
    } yield (c.name, s.name)
  }

  // DBIO Action which runs several queries inserting sample data
  val insertSupplierAndCoffeeData = DBIO.seq(
    Tables.suppliers += (101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199"),
    Tables.suppliers += (49, "Superior Coffee", "1 Party Place", "Mendocino", "CA", "95460"),
    Tables.suppliers += (150, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966"),
    Tables.coffees ++= Seq(
      ("Colombian", 101, 7.99, 0, 0),
      ("French_Roast", 49, 8.99, 0, 0),
      ("Espresso", 150, 9.99, 0, 0),
      ("Colombian_Decaf", 101, 8.99, 0, 0),
      ("French_Roast_Decaf", 49, 9.99, 0, 0)
    )
  )

  // DBIO Action which creates the schema
  val createSchemaAction = (suppliers.schema ++ coffees.schema).create

  // DBIO Action which drops the schema
  val dropSchemaAction = (suppliers.schema ++ coffees.schema).drop

  // Create database, composing create schema and insert sample data actions
  val createDatabase = DBIO.seq(createSchemaAction, insertSupplierAndCoffeeData)

}

trait SlickRoutes extends ScalatraBase with FutureSupport {

  def db: Database

  get("/db/create-db") {
    db.run(Tables.createDatabase)
  }

  get("/db/drop-db") {
    db.run(Tables.dropSchemaAction)
  }

  get("/coffees") {
    db.run(Tables.findCoffeesWithSuppliers.result) map { xs =>
      println(xs)
      contentType = "text/plain"
      xs map { case (s1, s2) => f"  $s1 supplied by $s2" } mkString "\n"
    }
  }

}

class SlickApp(val db: Database) extends ScalatraServlet with FutureSupport with SlickRoutes {

  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

}