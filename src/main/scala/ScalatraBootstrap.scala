import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.LoggerFactory
import com.toricor.demo._
import org.scalatra._
import javax.servlet.ServletContext

import slick.jdbc.H2Profile.api._

/**
  * This is the ScalatraBootstrap bootstrap file. You can use it to mount servlets or
  * filters. It's also a good place to put initialization code which needs to
  * run at application start (e.g. database configurations), and init params.
  */
class ScalatraBootstrap extends LifeCycle {

  val logger = LoggerFactory.getLogger(getClass)

  val cpds = new ComboPooledDataSource
  logger.info("Created c3p0 connection pool")

  override def init(context: ServletContext) {
    val db = Database.forDataSource(cpds, None)   // create the Database object
    context.mount(new FlowersController, "/flowers/*")
    context.mount(new SlickApp(db), "/*") // create and mount the Scalatra application

  }

  private def closeDbConnection() {
    logger.info("Closing c3po connection pool")
    cpds.close
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    closeDbConnection
  }
}